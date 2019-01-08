package com.opticdev.runtime

import better.files.File
import com.opticdev.common.graph.CommonAstNode
import com.opticdev.common.graph.path.WalkablePath
import com.opticdev.core.sourcegear.CompiledLens
import com.opticdev.core.sourcegear.graph.model.{ExpandedModelNode, FlatModelNode, LinkedModelNode}
import com.opticdev.core.sourcegear.project.OpticProject
import com.opticdev.core.sourcegear.snapshot.Snapshot
import com.opticdev.sdk.skills_sdk.LensRef
import com.opticdev.sdk.skills_sdk.lens.SchemaDef
import com.opticdev.marvin.common.helpers.InRangeImplicits._

import scala.util.Try

import scala.concurrent.Future

object RuntimeManager {
  /* Session Controller */
  def isCollecting = _session.isDefined && _session.get.isCollecting
  private var _session: Option[RuntimeCollectionSession] = None
  def session: Option[RuntimeCollectionSession] = _session
  def receiveFragment(runtimeValueFragment: RuntimeValueFragment) = session.foreach(_.receiveFragment(runtimeValueFragment))
  def newSession(project: OpticProject): Future[Unit] = {
    require(project.projectFile.interface.testcmd.isDefined, "No 'testcmd' in optic.yml file")
    require(_session.isEmpty, "Runtime session is already in progress.")
    _session = Some(new RuntimeCollectionSession(project))
    _session.get.start(() => _session = None)
  }
  def finish(keepLocked: Boolean = false): Try[RuntimeSessionResult] = Try(session.get.finish(keepLocked))

  /* Processing code */

  def stageSourceWrappers(snapshot: Snapshot): Vector[Try[WrapRequest]] = {
    val lensesIncludingListeners = snapshot.sourceGear.lensSet.listLenses.collect{ case baseLens: CompiledLens if baseLens.runtimeListener.nonEmpty => baseLens }
    val targetRuntimeNodes: Map[LensRef, Map[FlatModelNode, ExpandedModelNode]] = snapshot.linkedModelNodes.filter(model => lensesIncludingListeners.exists(_.lensRef == model._1.lensRef)).groupBy(_._1.lensRef)

    lensesIncludingListeners.toVector.flatMap{ case lens =>
      val runtimeListener = lens.runtimeListener

      targetRuntimeNodes.getOrElse(lens.lensRef, Map()).collect{ case i if i._2.isInstanceOf[LinkedModelNode[CommonAstNode]] => {
        val parentLinked = i._2.asInstanceOf[LinkedModelNode[CommonAstNode]]
        val file = snapshot.files(i._1).toFile

        runtimeListener.runtimeComponents.map{ case (componentWithPropertyPath, flatWalkablePath) => Try {
          val fileRecord = (snapshot.astGraphs.get)(file)
          val astGraph = fileRecord.astGraph
          val targetNode = WalkablePath(parentLinked.root, flatWalkablePath.path, astGraph).walk(parentLinked.root, astGraph)
          WrapRequest(parentLinked.root, targetNode, i._1.hash, i._1.schemaId, file, componentWithPropertyPath.component.processAs == SchemaDef, componentWithPropertyPath.component.options, componentWithPropertyPath)
        }}

      }}.flatten
    }
  }

  def collectFileChanges(wrapRequest: Vector[WrapRequest], snapshot: Snapshot): Vector[TempFilePatch] = {
    val groupedByFile = wrapRequest.groupBy(_.file)
    groupedByFile.map{
      case (file, wrapRequests) => {
        val fileContents = snapshot.astGraphs.get(file).fileContents
        val stringBuilder = new StringBuilder(fileContents)
        val sortedAsc = wrapRequests.sortBy(_.targetNode.range.start).reverse
        //assumes all nodes in the same file use the same parser...for now that's safe
        val parser = snapshot.sourceGear.parsers.find(_.languageName == wrapRequests.head.targetNode.nodeType.language).get

        sortedAsc.foreach(wrap => {
          val newContents = parser.runtimeHelper.get.generateCall(wrap.hash, wrap.componentWithPropertyPath.component.identifier, true, wrap.componentWithPropertyPath.component.options, fileContents.substring(wrap.targetNode.range))
          stringBuilder.updateRange(wrap.targetNode.range, newContents)
        })

        stringBuilder.updateRange(Range(0, 0), parser.runtimeHelper.get.importLine+"\n\n") // add import line to the top

        TempFilePatch(file, stringBuilder.mkString, fileContents)
      }
    }.toVector
  }

  def applyFilePatches(filePatches: Vector[TempFilePatch]): Unit = {
    filePatches.foreach(i => Try(i.file.write(i.newContents)))
  }

  def revertFilePatches(filePatches: Vector[TempFilePatch]): Unit =
    filePatches.foreach(i => Try(i.file.write(i.originalContents)))

}
