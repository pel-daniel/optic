package com.opticdev

import better.files.File
import com.opticdev.common.SchemaRef
import com.opticdev.common.graph.CommonAstNode
import com.opticdev.common.spec_types.IncompleteTestCoverage
import com.opticdev.core.sourcegear.graph.model.{ExpandedModelNode, FlatModelNode, LinkedModelNode}
import com.opticdev.core.sourcegear.project.{OpticProject, Project}
import com.opticdev.core.sourcegear.snapshot.Snapshot
import com.opticdev.sdk.skills_sdk.lens.{OMComponentWithPropertyPath, OMLensRuntimeComponent}
import play.api.libs.json.JsObject
import com.opticdev.marvin.common.helpers.InRangeImplicits._

package object runtime {

  val apiTypes = Map(
    "apiatlas:schemas/response" -> "Response",
    "apiatlas:schemas/request-body" -> "Request Body",
    "apiatlas:schemas/endpoint" -> "Endpoint",
  )

  case class RuntimeSessionResult(runtimeFragments: Vector[RuntimeValueFragment], issues: Vector[IncompleteTestCoverage], totalListeners: Int, coverage: Int, totalFragments: Int)

  case class WrapRequest(modelParentNode: CommonAstNode, targetNode: CommonAstNode, hash: String, schemaRef: SchemaRef, file: File, inferSchema: Boolean, options: JsObject, componentWithPropertyPath: OMComponentWithPropertyPath[OMLensRuntimeComponent]) {
    def toRuntimeTarget(snapshot: Snapshot) = {
      val fileContents = snapshot.contextForNode.find(_._2.file == file).map(_._2.fileContents).get
      val contents = fileContents.substring(modelParentNode.range)
      RuntimeTarget(schemaRef, file.toString(), modelParentNode.lineRange(fileContents), hash, contents)
    }
  }
  case class TempFilePatch(file: File, newContents: String, originalContents: String)

  case class RuntimeTarget(schemaRef: SchemaRef, file: String, lineRange: Range, modelHash: String, contents: String)
  class RuntimeIncidenceTracker(targets: RuntimeTarget*) {
    private val _targets = collection.mutable.HashMap[RuntimeTarget, Int](targets.map(i => (i, 0)):_*)

    def mark(modelHash: String) =
      _targets.find(_._1.modelHash == modelHash)
      .foreach(i => _targets.put(i._1, i._2 + 1))

    def results = _targets.toMap

    def issues(project: OpticProject) = {
      _targets.filter(i => i._2 == 0 && apiTypes.contains(i._1.schemaRef.internalFull) ).map(target => {
        val trimmed = project.trimAbsoluteFilePath(target._1.file)
        IncompleteTestCoverage.from(apiTypes(target._1.schemaRef.internalFull), trimmed, target._1.lineRange, target._1.contents)
      }).toVector
    }


  }

}
