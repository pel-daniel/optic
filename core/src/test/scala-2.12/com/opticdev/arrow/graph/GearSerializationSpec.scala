package com.opticdev.arrow.graph

import com.opticdev.arrow.ExampleSourcegears
import com.opticdev.arrow.index.IndexSourceGear
import com.opticdev.core.Fixture.TestBase
import com.opticdev.core.sourcegear.CompiledLens
import com.opticdev.opm.TestPackageProviders
import com.opticdev.common.SchemaRef
import org.scalatest.FunSpec
import play.api.libs.json.Json
import scalax.collection.edge.LkDiEdge

import scala.reflect.ClassTag

class GearSerializationSpec extends TestBase with TestPackageProviders {

  lazy val exampleProjectSG = ExampleSourcegears.exampleProjectSG.sourceGear

  it("can turn a gear node into json") {
    val json = GraphSerialization.jsonFromNode(LensNode(exampleProjectSG.lensSet.listLenses.find(_.id == "express-parameter").get))(exampleProjectSG)
    assert(json == Json.parse("""{"id":"apiatlas:flat-express-js@0.0.1/express-parameter","schemaRef":"apiatlas:flat-express-js@0.0.1/parameter","schema":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}},"name":"express-parameter","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"}"""))
  }

  it("can turn a schema node into json") {
    val json = GraphSerialization.jsonFromNode(SchemaNode(exampleProjectSG.schemas.find(_.name.contains("Parameter")).get))(exampleProjectSG)
    assert(json == Json.parse("""{"id":"apiatlas:flat-express-js@0.0.1/parameter","name":"Parameter","schema":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}},"packageFull":"apiatlas:flat-express-js@0.0.1","type":"schema"}"""))
  }


  it("can serialize a basic graph") {
    val graph = IndexSourceGear.runFor(exampleProjectSG)
    val result = GraphSerialization.serialize(graph)(exampleProjectSG)
    assert(result == Json.parse("""{"nodes":[{"id":"apiatlas:flat-express-js@0.0.1/express-endpoint-reffed","schemaRef":"apiatlas:flat-express-js@0.0.1/endpoint","schema":{"title":"Endpoint","type":"object","required":["url","method"],"additionalProperties":false,"properties":{"url":{"type":"string"},"method":{"type":"string","enum":["get","post","put","delete","options","head"]},"parameters":{"type":"array","items":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}}},"body":{"title":"Request Body","type":"object","properties":{"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false},"responses":{"type":"array","items":{"title":"Response","type":"object","required":["status"],"properties":{"status":{"type":"number"},"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false}}}},"name":"Express Endpoint","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"},{"id":"apiatlas:flat-express-js@0.0.1/express-endpoint","schemaRef":"apiatlas:flat-express-js@0.0.1/endpoint","schema":{"title":"Endpoint","type":"object","required":["url","method"],"additionalProperties":false,"properties":{"url":{"type":"string"},"method":{"type":"string","enum":["get","post","put","delete","options","head"]},"parameters":{"type":"array","items":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}}},"body":{"title":"Request Body","type":"object","properties":{"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false},"responses":{"type":"array","items":{"title":"Response","type":"object","required":["status"],"properties":{"status":{"type":"number"},"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false}}}},"name":"Express Endpoint","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"},{"id":"apiatlas:flat-express-js@0.0.1/express-header-function-style","schemaRef":"apiatlas:flat-express-js@0.0.1/parameter","schema":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}},"name":"express-header-function-style","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"},{"id":"apiatlas:flat-express-js@0.0.1/express-handler","schemaRef":"apiatlas:flat-express-js@0.0.1/express-handler","schema":{"_id":null,"_definition":{"title":"Express Handler","type":"object","required":["parameters","responses"],"properties":{}},"title":"Express Handler"},"name":"Express Handler","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"},{"id":"apiatlas:flat-express-js@0.0.1/express-parameter","schemaRef":"apiatlas:flat-express-js@0.0.1/parameter","schema":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}},"name":"express-parameter","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"},{"id":"host-lang:es7@1.0.0/named-import","name":"named-import","schema":{"type":"object","properties":{"imported":{"type":"string"},"local":{"type":"string"},"path":{"type":"string"}}},"packageFull":"host-lang:es7@1.0.0","type":"schema"},{"id":"apiatlas:flat-express-js@0.0.1/express-header-bracket-style","schemaRef":"apiatlas:flat-express-js@0.0.1/parameter","schema":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}},"name":"express-header-bracket-style","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"},{"id":"host-lang:es7@1.0.0/es-import-statement","schemaRef":"host-lang:es7@1.0.0/multiple-import","schema":{"type":"object","properties":{"imports":{"type":"array","items":{"type":"object","properties":{"imported":{"type":"string"},"local":{"type":"string"},"path":{"type":"string"}}}}}},"name":"es-import-statement","packageFull":"host-lang:es7@1.0.0","internal":true,"priority":1,"type":"lens"},{"id":"host-lang:es7@1.0.0/require-import-statement","schemaRef":"host-lang:es7@1.0.0/named-import","schema":{"type":"object","properties":{"imported":{"type":"string"},"local":{"type":"string"},"path":{"type":"string"}}},"name":"require-import-statement","packageFull":"host-lang:es7@1.0.0","internal":true,"priority":1,"type":"lens"},{"id":"apiatlas:flat-express-js@0.0.1/parameter","name":"Parameter","schema":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}},"packageFull":"apiatlas:flat-express-js@0.0.1","type":"schema"},{"id":"apiatlas:flat-express-js@0.0.1/response","name":"Response","schema":{"title":"Response","type":"object","required":["status"],"properties":{"status":{"type":"number"},"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false},"packageFull":"apiatlas:flat-express-js@0.0.1","type":"schema"},{"id":"apiatlas:flat-express-js@0.0.1/status-code-response","schemaRef":"apiatlas:flat-express-js@0.0.1/response","schema":{"title":"Response","type":"object","required":["status"],"properties":{"status":{"type":"number"},"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false},"name":"status-code-response","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"},{"id":"apiatlas:flat-express-js@0.0.1/endpoint","name":"Endpoint","schema":{"title":"Endpoint","type":"object","required":["url","method"],"additionalProperties":false,"properties":{"url":{"type":"string"},"method":{"type":"string","enum":["get","post","put","delete","options","head"]},"parameters":{"type":"array","items":{"title":"Parameter","type":"object","required":["in","name"],"additionalProperties":false,"properties":{"in":{"type":"string","enum":["path","query","header","cookie"]},"name":{"type":"string"},"required":{"type":"boolean"},"schema":{"type":"object"}}}},"body":{"title":"Request Body","type":"object","properties":{"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false},"responses":{"type":"array","items":{"title":"Response","type":"object","required":["status"],"properties":{"status":{"type":"number"},"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false}}}},"packageFull":"apiatlas:flat-express-js@0.0.1","type":"schema"},{"id":"host-lang:es7@1.0.0/multiple-import","name":"multiple-import","schema":{"type":"object","properties":{"imports":{"type":"array","items":{"type":"object","properties":{"imported":{"type":"string"},"local":{"type":"string"},"path":{"type":"string"}}}}}},"packageFull":"host-lang:es7@1.0.0","type":"schema"},{"id":"apiatlas:flat-express-js@0.0.1/express-handler","name":"Express Handler","schema":{"_id":null,"_definition":{"title":"Express Handler","type":"object","required":["parameters","responses"],"properties":{}},"title":"Express Handler"},"packageFull":"apiatlas:flat-express-js@0.0.1","type":"schema"},{"id":"apiatlas:flat-express-js@0.0.1/express-response-default","schemaRef":"apiatlas:flat-express-js@0.0.1/response","schema":{"title":"Response","type":"object","required":["status"],"properties":{"status":{"type":"number"},"content-type":{"type":"string","enum":["application/json","application/xml","application/x-www-form-urlencoded","text/plain"]},"schema":{"type":"object"}},"additionalProperties":false},"name":"express-response-default","packageFull":"apiatlas:flat-express-js@0.0.1","internal":false,"priority":1,"type":"lens"}],"edges":[{"n1":"host-lang:es7@1.0.0/named-import","n2":"host-lang:es7@1.0.0/require-import-statement"},{"n1":"apiatlas:flat-express-js@0.0.1/parameter","n2":"apiatlas:flat-express-js@0.0.1/express-parameter"},{"n1":"apiatlas:flat-express-js@0.0.1/parameter","n2":"apiatlas:flat-express-js@0.0.1/express-header-bracket-style"},{"n1":"apiatlas:flat-express-js@0.0.1/parameter","n2":"apiatlas:flat-express-js@0.0.1/express-header-function-style"},{"n1":"apiatlas:flat-express-js@0.0.1/response","n2":"apiatlas:flat-express-js@0.0.1/express-response-default"},{"n1":"apiatlas:flat-express-js@0.0.1/response","n2":"apiatlas:flat-express-js@0.0.1/status-code-response"},{"n1":"apiatlas:flat-express-js@0.0.1/endpoint","n2":"apiatlas:flat-express-js@0.0.1/express-endpoint"},{"n1":"apiatlas:flat-express-js@0.0.1/endpoint","n2":"apiatlas:flat-express-js@0.0.1/express-endpoint-reffed"},{"n1":"host-lang:es7@1.0.0/multiple-import","n2":"host-lang:es7@1.0.0/es-import-statement"},{"n1":"apiatlas:flat-express-js@0.0.1/express-handler","n2":"apiatlas:flat-express-js@0.0.1/express-handler"}]}"""))
  }

  it("can serialize a graph with transformations") {
    val exampleProjectSG = ExampleSourcegears.sgWithTransformations.sourceGear
    val graph = IndexSourceGear.runFor(exampleProjectSG)
    val result = GraphSerialization.serialize(graph)(exampleProjectSG)

    assert(result == Json.parse("""{"nodes":[{"id":"optic:test@0.1.0/model","name":"model","schema":{},"packageFull":"optic:test@0.1.0","type":"schema"},{"id":"optic:test@0.1.0/route","name":"route","schema":{},"packageFull":"optic:test@0.1.0","type":"schema"},{"id":"optic:test@0.1.0/form","name":"form","schema":{},"packageFull":"optic:test@0.1.0","type":"schema"},{"id":"optic:test@0.1.0/fetch","name":"fetch","schema":{},"packageFull":"optic:test@0.1.0","type":"schema"}],"edges":[{"from":"optic:test@0.1.0/model","fromName":"model","toName":"route","to":"optic:test@0.1.0/route","label":{"name":"Model -> Route","packageFull":"optic:test-transform@latest"},"transformationRef":"optic:test-transform@latest/m2r","isTransformation":true},{"from":"optic:test@0.1.0/route","fromName":"route","toName":"form","to":"optic:test@0.1.0/form","label":{"name":"Route -> Form","packageFull":"optic:test-transform@latest"},"transformationRef":"optic:test-transform@latest/r2f","isTransformation":true},{"from":"optic:test@0.1.0/route","fromName":"route","toName":"fetch","to":"optic:test@0.1.0/fetch","label":{"name":"Route -> Fetch","packageFull":"optic:test-transform@latest"},"transformationRef":"optic:test-transform@latest/r2fe","isTransformation":true}]}"""))
  }


}
