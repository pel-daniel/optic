package com.opticdev.core.sourcegear.project

import java.nio.file.{StandardWatchEventKinds => EventType}

import better.files.File
import com.opticdev.core.sourcegear.actors._
import com.opticdev.core.sourcegear.project.config.ProjectFile
import com.opticdev.core.sourcegear.project.status._
import com.opticdev.core.sourcegear.{SGConstructor, SourceGear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class Project(name: String, baseDirectory: File)(implicit logToCli: Boolean = false, actorCluster: ActorCluster) extends OpticProject(name, baseDirectory) {

  private var sourceGear: SourceGear = {
    projectStatusInstance.sourceGearStatus = Building
    SourceGear.unloaded
  }

  //do this after sourcegear is initialized

  override def projectFileChanged(newPf: ProjectFile): Unit = {
    super.projectFileChanged(newPf)
    regenerateSourceGear(newPf)
  }

  override def regenerateSourceGear(newPf: ProjectFile) = {
    if (newPf.interface.isSuccess) {
      projectStatusInstance.sourceGearStatus = Building
      SGConstructor.fromProjectFile(newPf)(useCache = true).onComplete(i => {
        if (i.isSuccess) {
          sourceGear = i.get.inflate
          //run all callbacks
          sourcegearchangedCallbacks.foreach(_.apply(sourceGear))
          projectStatusInstance.sourceGearStatus = Valid
          sourceGear.print
          if (projectStatus.monitoringStatus == Watching) rereadAll
        } else {
          i.failed.get.printStackTrace()
          projectStatusInstance.sourceGearStatus = Invalid(i.failed.get.getMessage)
        }
      })
    }
  }

  override def projectSourcegear = synchronized {
    sourceGear
  }

  private val sourcegearchangedCallbacks = scala.collection.mutable.ListBuffer[(SourceGear)=> Unit]()
  override def onSourcegearChanged(callback: (SourceGear)=> Unit) : Unit =
    if (callback != null) sourcegearchangedCallbacks += callback

}

object Project {
  def fromProjectFile(pf: ProjectFile)(implicit actorCluster: ActorCluster) : Try[OpticProject] = Try {
    new Project(pf.name.get, pf.file.parent)
  }
}