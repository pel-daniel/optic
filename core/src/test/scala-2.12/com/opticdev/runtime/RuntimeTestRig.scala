package com.opticdev.runtime

import akka.actor.ActorSystem
import better.files.File
import com.opticdev.core.Fixture.TestBase
import com.opticdev.core.Fixture.compilerUtils.GearUtils
import com.opticdev.core.sourcegear.actors.ActorCluster
import com.opticdev.core.sourcegear.project.StaticSGProject
import com.opticdev.core.sourcegear.snapshot.Snapshot
import scala.concurrent.duration._

import scala.concurrent.Await

trait RuntimeTestRig extends TestBase with GearUtils {

  lazy val runtimeTestSourceGear = sourceGearFromDescription("test-examples/resources/example_packages/runtimetest.json")

  def fixture(filePath: String) = new {
    val file = File(filePath)
    implicit val actorCluster: ActorCluster = new ActorCluster(ActorSystem())
    implicit val project = new StaticSGProject("test", File(getCurrentDirectory + "/test-examples/"), runtimeTestSourceGear)
    val results = runtimeTestSourceGear.parseFile(file).get

    project.projectGraphWrapper.addFile(results.astGraph, file, results.fileTokenRegistry.exports)
    val snapshot = {
      Await.result(Snapshot.forSourceGearAndProjectGraph(runtimeTestSourceGear, project.projectGraphWrapper.projectGraph, actorCluster.parserSupervisorRef, project, includeAstGraphs = true), 30 seconds)
    }
  }


}
