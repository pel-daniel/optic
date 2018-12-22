package com.opticdev.runtime

import com.opticdev.core.sourcegear.project.OpticProject
import com.opticdev.core.sourcegear.snapshot.Snapshot
import com.opticdev.core.sourcegear.storage.ProjectRuntimeFragmentStorage

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

class RuntimeCollectionSession(project: OpticProject) {
  private var _isCollecting: Boolean = false
  def isCollecting: Boolean = _isCollecting

  private val _mutableList = scala.collection.mutable.ListBuffer[RuntimeValueFragment]()

  def receiveFragment(runtimeValueFragment: RuntimeValueFragment): Unit = _mutableList.append(runtimeValueFragment)

  private var _filePatches: Vector[TempFilePatch] = Vector()
  private var _clearSession: () => Unit = null

  def start(clearSession: () => Unit) = {
    _clearSession = clearSession
    project.setRuntimeFragments(Vector())
    ProjectRuntimeFragmentStorage.clearStorage(project.name)

    val setupFuture: Future[(Snapshot, Vector[Try[WrapRequest]], Vector[TempFilePatch])] = for {
     _ <- project.onFirstPassComplete //makes sure project is fully loaded before running
     snapshot <- project.snapshot(withAstGraph = true)
     wrappers <- Future(RuntimeManager.stageSourceWrappers(snapshot))
     filePatches <- Future(RuntimeManager.collectFileChanges(wrappers.collect{case a if a.isSuccess => a.get}, snapshot))
    } yield (snapshot, wrappers, filePatches)

    setupFuture.map {
      case (snapshot, wrappers, filePatches) => {
        _filePatches = filePatches //save file patches in case we need to abort
        RuntimeManager.applyFilePatches(filePatches)
        _isCollecting = true
      }
    }
  }

  def finish: Vector[RuntimeValueFragment] = {
    RuntimeManager.revertFilePatches(_filePatches)
    val fragments = _mutableList.toVector
    ProjectRuntimeFragmentStorage.addManyToStorage(fragments, project.name)
    project.setRuntimeFragments(fragments)
    _isCollecting = false
    _clearSession()
    println(fragments)
    fragments
  }

}
