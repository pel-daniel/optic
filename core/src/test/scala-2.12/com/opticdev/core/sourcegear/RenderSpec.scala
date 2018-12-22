package com.opticdev.core.sourcegear

import com.opticdev.common.{PackageRef, SchemaRef}
import com.opticdev.core.Fixture.compilerUtils.{GearUtils, ParserUtils}
import com.opticdev.core.Fixture.{DummyCompilerOutputs, ExampleSourcegearFixtures, TestBase}
import com.opticdev.core.compiler.stages.RenderFactoryStage
import com.opticdev.core.sourcegear.context.FlatContext
import com.opticdev.core.sourcegear.graph.ProjectGraph
import com.opticdev.parsers.{ParserBase, SourceParserManager}
import com.opticdev.runtime.RuntimeSourceListener
import com.opticdev.sdk.descriptions.enums.FinderEnums.{Containing, Entire}
import com.opticdev.sdk.descriptions._
import com.opticdev.sdk.descriptions.enums.RuleEnums
import com.opticdev.sdk.descriptions.transformation.Transformation
import com.opticdev.sdk.descriptions.transformation.generate.{RenderOptions, StagedNode}
import com.opticdev.sdk.skills_sdk.LensRef
import com.opticdev.sdk.skills_sdk.schema.OMSchema
import org.scalatest.PrivateMethodTester
import play.api.libs.json.{JsObject, JsString}

class RenderSpec extends TestBase with PrivateMethodTester with GearUtils with ParserUtils {
  describe("uses the proper gear") {

    lazy val testSchemaRef = SchemaRef.fromString("test:schemas@0.1.0/a").get

    lazy val a = CompiledLens(Some("test"), "test", PackageRef.fromString("optic:test@0.1.0").get, Left(testSchemaRef), Set(), DummyCompilerOutputs.parser, DummyCompilerOutputs.render, RuntimeSourceListener(Map()), 1)
    lazy val b = CompiledLens(Some("other"), "other", PackageRef.fromString("optic:test@0.1.0").get, Left(testSchemaRef), Set(), DummyCompilerOutputs.parser, DummyCompilerOutputs.render, RuntimeSourceListener(Map()), 1)


    val testSchema = OMSchema(testSchemaRef, JsObject.empty)

    val sourceGear = new SourceGear {
      override val configHash: String = "test-rig"
      override val parsers: Set[ParserBase] = Set()
      override val lensSet: LensSet = new LensSet(a, b)
      override val transformations: Set[Transformation] = Set()
      override val schemas: Set[OMSchema] = Set(testSchema)
      override val connectedProjectGraphs: Set[ProjectGraph] = Set()
      override val flatContext: FlatContext = FlatContext(None, Map(
        "optic:test" -> FlatContext(Some(a.packageRef), Map(
          "test" -> a,
          "other" -> b
        )),
        "test:schemas" -> FlatContext(Some(a.packageRef), Map(
          "a" -> testSchema
        ))
      ))
    }

    lazy val resolveLens = PrivateMethod[Option[CompiledLens]]('resolveLens)

    it("if set in options") {
      LensRef.fromString(a.id, Some(a.packageRef))
      val stagedNode = StagedNode(testSchemaRef, JsObject.empty, Some(RenderOptions(generatorId = Some(LensRef.fromString(a.id, Some(a.packageRef)).get.full))))
      val result = Render invokePrivate resolveLens(stagedNode, sourceGear, sourceGear.flatContext)
      assert(result.contains(a))
    }

    it("if not set in options gets first matching") {
      val stagedNode = StagedNode(testSchemaRef, JsObject.empty)
      val result = Render invokePrivate resolveLens(stagedNode, sourceGear, sourceGear.flatContext)
      assert(result.contains(a))
    }

    it("will return none if gear is not found") {
      val stagedNode = StagedNode(testSchemaRef, JsObject.empty, Some(RenderOptions(generatorId = Some("FAKE"))))
      val result = Render invokePrivate resolveLens(stagedNode, sourceGear, sourceGear.flatContext)
      assert(result.isEmpty)
    }

  }

  it("can render a flat node") {

    implicit val sourceGear = sourceGearFromDescription("test-examples/resources/example_packages/optic:ImportExample@0.1.0.json")

    val result = Render.simpleNode(SchemaRef(Some(PackageRef("optic:importexample")), "js-import"), JsObject(
      Seq("definedAs" -> JsString("ABC"), "pathTo" -> JsString("DEF"))
    ))

    assert(result.get._2 == """let ABC = require('DEF')""")

  }

  it("can render node with nested gears and variables") {

    import ExampleSourcegearFixtures.routeQueryResponse
    val f = routeQueryResponse

    val queryValue = JsObject(Seq(
      "fields" -> JsObject(Seq("fieldA" -> JsObject(Seq("_valueFormat" -> JsString("code"), "value" -> JsString("req.query.fieldA")))))
    ))

    val stagedNode = StagedNode(f.routeGear.schemaRef, JsObject.empty, Some(RenderOptions(
      variables = Some(Map("request" -> "req", "response" -> "res")),
      containers = Some(Map("callback" -> Seq(
        StagedNode(f.queryGear.schemaRef, queryValue, Some(RenderOptions(
          containers = Some(Map(
            "success" -> Seq(
              StagedNode(f.responseGear.schemaRef, JsObject.empty)
            )
          ))
        )))
      )))
    )))

    val result = Render.fromStagedNode(stagedNode)(f.sourceGear, f.sourceGear.flatContext)

    val expected = "call(\"value\", function (req, res) {\n  \n  query({ fieldA: req.query.fieldA }, function (err, item) {\n    if (!err) {\n        res.send(thing)\n    } else {\n    \n    }\n  })\n})"
    assert(result.get._2 == expected)

  }

  it("will add tags to generated code") {
    import ExampleSourcegearFixtures.routeQueryResponse
    val f = routeQueryResponse

    val queryValue = JsObject(Seq(
      "fields" -> JsObject(Seq("fieldA" -> JsObject(Seq("_valueFormat" -> JsString("code"), "value" -> JsString("req.query.fieldA")))))
    ))

    val stagedNode = StagedNode(f.routeGear.schemaRef, JsObject.empty, Some(RenderOptions(
      variables = Some(Map("request" -> "req", "response" -> "res")),
      containers = Some(Map("callback" -> Seq(
        StagedNode(f.queryGear.schemaRef, queryValue, Some(RenderOptions(
          containers = Some(Map(
            "success" -> Seq(
              StagedNode(f.responseGear.schemaRef, JsObject.empty)
            )
          )),
          tag = Some("query")
        )))
      )))
    )))


    val result = Render.fromStagedNode(stagedNode)(f.sourceGear, f.sourceGear.flatContext)

    val expected = "call(\"value\", function (req, res) {\n  \n  query({ fieldA: req.query.fieldA }, function (err, item) {  //optic.tag = \"query\"\n    if (!err) {\n        res.send(thing)\n    } else {\n    \n    }\n  })\n})"
    assert(result.get._2 == expected)

  }

  it("can render a node from builtin lens") {

    implicit val sourceGear = sourceGearFromDescription("test-examples/resources/example_packages/optic:ImportExample@0.1.0.json")

    val result = Render.simpleNode(SchemaRef(Some(PackageRef("optic:builtins")), "raw"), JsObject(
      Seq("rawText" -> JsString("raw.rendered()"))
    ))

    assert(result.get._2 == """raw.rendered()""")

  }

}
