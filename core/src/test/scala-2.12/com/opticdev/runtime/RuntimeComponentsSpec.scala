package com.opticdev.runtime

import better.files.File
import com.opticdev.common.graph.Child
import com.opticdev.core.Fixture.{AkkaTestFixture, TestBase}
import com.opticdev.core.Fixture.compilerUtils.{GearUtils, ParserUtils}
import com.opticdev.core.sourcegear.context.FlatContext
import com.opticdev.core.sourcegear.graph.ProjectGraph
import com.opticdev.core.sourcegear.project.StaticSGProject
import com.opticdev.core.sourcegear.{LensSet, SourceGear}
import com.opticdev.parsers.{ParserBase, SourceParserManager}
import com.opticdev.sdk.descriptions.enums.FinderEnums.Entire
import com.opticdev.sdk.skills_sdk.lens._

class RuntimeComponentsSpec extends AkkaTestFixture("RuntimeComponentsSpec") with ParserUtils {

  implicit val sourceGear = new SourceGear {
  override val configHash: String = "test-rig"
  override val parsers: Set[ParserBase] = SourceParserManager.installedParsers
  override val lensSet = new LensSet()
  override val schemas = Set()
  override val transformations = Set()
  override val flatContext: FlatContext = FlatContext(None, Map.empty)
  override val connectedProjectGraphs: Set[ProjectGraph] = Set()
}

  implicit val project = new StaticSGProject("test", File(getCurrentDirectory + "/test-examples/resources/tmp/test_project/"), sourceGear)

  def simpleFixture = new {
    val (parseGear, finderOutput, parserOutput, lens) = parseGearFromSnippetWithComponentsAndIntermediates("res.send(abc)", Map(
      //this causes any token rule to be applied
      "runtimeField" -> OMLensRuntimeComponent(OMStringFinder(Entire, "abc"), ObjectDef)
    ))
  }

  it("will setup rules properly around token") {
    val f = simpleFixture

    val block = "res.send(canBeAnyToken)"

    val parsedSample = sample(block)
    val result = f.parseGear.matches(parsedSample.entryChildren.head, true)(parsedSample.astGraph, block, sourceGearContext, project)
    assert(result.isDefined)
  }

  it("can compile into runtime listeners") {
    val f = simpleFixture
    val runtimeListener = RuntimeSourceListener.from(f.finderOutput, f.parserOutput, f.lens.runtimeFieldComponentsCompilerInput)

    assert(runtimeListener.runtimeComponents.size == 1)
    assert(runtimeListener.runtimeComponents.head._2.path.head == Child(0, "arguments"))
  }



}
