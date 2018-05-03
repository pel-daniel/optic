package com.opticdev.core.sourcegear.sync

import better.files.File
import com.opticdev.core.Fixture.AkkaTestFixture
import com.opticdev.core.Fixture.compilerUtils.GearUtils
import com.opticdev.core.sourcegear.project.StaticSGProject
import play.api.libs.json.Json

class SyncPatchSpec extends AkkaTestFixture("GraphFunctionsSpec") with GearUtils {

  lazy val syncTestSourceGear = sourceGearFromDescription("test-examples/resources/example_packages/synctest.json")

  def fixture(filePath: String) = new {
    val file = File(filePath)
    implicit val project = new StaticSGProject("test", File(getCurrentDirectory + "/test-examples/resources/tmp/test_project/"), syncTestSourceGear)
    val results = syncTestSourceGear.parseFile(file).get
    val updatedGraphResults = {
      project.projectGraphWrapper.addFile(results.astGraph, file)
      SyncGraphFunctions.updateSyncEdges(results.astGraph)
    }
  }

  it("can calculate valid file patch from diff") {

    val f = fixture("test-examples/resources/example_source/sync/Sync.js")
    implicit val project = f.project

    project.stageProjectGraph(f.updatedGraphResults.graph)
    val diff = DiffSyncGraph.calculateDiff(project)
    val filePatches = diff.filePatches

    assert(filePatches.head.newFileContents === """source('hello') //name: Hello Model
                                                  |source('good morning') //name: Good Morning
                                                  |source('welcome to') //name: Welcome To
                                                  |source('welcome to') //name: Welcome To
                                                  |
                                                  |target('hello') //source: Hello Model -> optic:synctest/passthrough-transform
                                                  |target('good morning') //source: Good Morning -> optic:synctest/passthrough-transform
                                                  |target('not_real') //source: Not Real -> optic:synctest/passthrough-transform""".stripMargin)

  }

}
