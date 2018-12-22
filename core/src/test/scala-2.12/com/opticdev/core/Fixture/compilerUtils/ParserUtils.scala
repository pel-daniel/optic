package com.opticdev.core.Fixture.compilerUtils

import com.opticdev.common.PackageRef
import com.opticdev.core.compiler.{FinderStageOutput, ParserFactoryOutput, SnippetStageOutput}
import com.opticdev.core.compiler.stages.{FinderStage, ParserFactoryStage, SnippetStage}
import com.opticdev.sdk.descriptions._
import com.opticdev.core.sourcegear.gears.parsing.{ParseAsModel, ParseGear}
import com.opticdev.core._
import com.opticdev.core.sourcegear.containers.SubContainerManager
import com.opticdev.core.sourcegear.variables.VariableManager
import com.opticdev.opm.context.{Tree, TreeContext}
import com.opticdev.parsers.SourceParserManager
import com.opticdev.sdk.skills_sdk.{OMChildrenRuleType, OMSnippet}
import com.opticdev.sdk.skills_sdk.lens.{OMLens, OMLensComponent, OMLensVariableScopeEnum}
import play.api.libs.json.JsObject

trait ParserUtils {

  def parseGearFromSnippetWithComponents(block: String, value: Map[String, OMLensComponent], subContainers: Map[String, OMChildrenRuleType] = Map(), variables: Map[String, OMLensVariableScopeEnum] = Map(), id: String = "example", schemaId: String = "BLANK") : (ParseAsModel, OMLens) = {
    val snippet = OMSnippet("es7", block)
    implicit val lens : OMLens = OMLens(
      Some("Example"),
      id,
      snippet,
      value,
      variables,
      subContainers,
      Left(BlankSchema(schemaId)),
      JsObject.empty,
      snippet.language,
      PackageRef("test:example", "0.1.1"))

    implicit val variableManager = VariableManager(lens.variablesCompilerInput, SourceParserManager.installedParsers.head.identifierNodeDesc)

    val snippetBuilder = new SnippetStage(snippet)
    val snippetOutput = snippetBuilder.run

    implicit val subcontainersManager = new SubContainerManager(lens.subcontainerCompilerInputs, snippetOutput.containerMapping)

    val finderStage = new FinderStage(snippetOutput)
    val finderStageOutput = finderStage.run

    val parserFactoryStage = new ParserFactoryStage(snippetOutput, finderStageOutput, None)(lens, variableManager, subcontainersManager)
    val output = parserFactoryStage.run

    (output.parseGear.asInstanceOf[ParseAsModel], lens)
  }

  def parseGearFromSnippetWithComponentsAndIntermediates(block: String, value: Map[String, OMLensComponent], subContainers: Map[String, OMChildrenRuleType] = Map(), variables: Map[String, OMLensVariableScopeEnum] = Map(), id: String = "example", schemaId: String = "BLANK"): (ParseAsModel, FinderStageOutput, ParserFactoryOutput, OMLens) = {
    val snippet = OMSnippet("es7", block)
    implicit val lens : OMLens = OMLens(
      Some("Example"),
      id,
      snippet,
      value,
      variables,
      subContainers,
      Left(BlankSchema(schemaId)),
      JsObject.empty,
      snippet.language,
      PackageRef("test:example", "0.1.1"))

    implicit val variableManager = VariableManager(lens.variablesCompilerInput, SourceParserManager.installedParsers.head.identifierNodeDesc)

    val snippetBuilder = new SnippetStage(snippet)
    val snippetOutput = snippetBuilder.run

    implicit val subcontainersManager = new SubContainerManager(lens.subcontainerCompilerInputs, snippetOutput.containerMapping)

    val finderStage = new FinderStage(snippetOutput)
    val finderStageOutput = finderStage.run

    val parserFactoryStage = new ParserFactoryStage(snippetOutput, finderStageOutput, None)(lens, variableManager, subcontainersManager)
    val output = parserFactoryStage.run

    (output.parseGear.asInstanceOf[ParseAsModel], finderStageOutput, output, lens)
  }

  def sample(block: String) : SnippetStageOutput = {
    val snippet = OMSnippet("es7", block)
    implicit val lens : OMLens = OMLens(Some("Example"), "example", snippet, Map(), Map(), Map(), Left(BlankSchema()), JsObject.empty, "es7", PackageRef("test:example", "0.1.1"))
    val snippetBuilder = new SnippetStage(snippet)
    snippetBuilder.run
  }


}
