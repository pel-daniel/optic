package com.opticdev.core.apis

import com.opticdev.common.spec_types.HTTPBearer
import com.opticdev.common.{PackageRef, SchemaRef}
import com.opticdev.core.sourcegear.annotations.NameAnnotation
import com.opticdev.core.sourcegear.graph.model.{FlatModelNode, ModelAnnotations, ModelNode}
import com.opticdev.core.sourcegear.graph.objects.ObjectNode
import com.opticdev.core.sourcegear.snapshot.Snapshot
import org.scalatest.FunSpec
import play.api.libs.json.{JsObject, JsString, Json}

class AssembleProjectSpecSpec extends FunSpec {
  val apiSchemaId = SchemaRef(Some(PackageRef("apiatlas:schemas")), "endpoint")
  def snapshotFixture(endpoints: Vector[(String, JsObject)]) = {

    val endpointMap = endpoints.map(i=> {
      val mn = ModelNode(apiSchemaId, JsObject.empty, JsObject.empty, null, 1, Map(), i._1, false)(null)
      mn.attachAnnotations(ModelAnnotations(Some(NameAnnotation(i._1, null, false)), None, None, Vector()))
      (mn.asInstanceOf[FlatModelNode], i._2)
    }).toMap

    Snapshot(null, null, Map(), endpointMap, Map(), Map(), Vector(), None)
  }

  def snapshotWithAuthFixture(endpoints: Vector[(String, JsObject)]) = {

    val endpointMap = endpoints.map(i=> {
      val mn = ModelNode(apiSchemaId, JsObject.empty, JsObject.empty, null, 1, Map(), i._1, false)(null)
      mn.attachAnnotations(ModelAnnotations(Some(NameAnnotation(i._1, null, false)), None, None, Vector()))
      (mn.asInstanceOf[FlatModelNode], i._2)
    }).toMap

    Snapshot(null, null, Map(), endpointMap, Map(), Map(),
      Vector(ObjectNode("MyAuth", Some(SchemaRef.fromString("apiatlas:schemas/authentication").get), JsObject(Seq("type" -> JsString("bearer")))))
      , None)
  }

  val exampleAPI = Vector(
    ("Hello", Json.parse(
      """
        |{
        |	"method": "post",
        |	"url": "/my-url",
        |	"parameters": [{"in": "query", "name": "test"}, {"in": "header", "name": "Authorization"}]
        |}
      """.stripMargin).as[JsObject])
  )

  it("can collect endpoints into spec") {
    val snapshot = snapshotFixture(exampleAPI)
    val result = AssembleProjectSpec.fromSnapshot(snapshot, None, "Project")
    assert(result.result.apiSpec.endpoints.size == 1)
  }




  it("can collect auth information from endpoints") {

    val snapshot = snapshotWithAuthFixture(exampleAPI)
    val result = AssembleProjectSpec.fromSnapshot(snapshot, None, "Project")

    val endpoint = result.result.apiSpec.endpoints.head

    assert(result.result.apiSpec.authenticationSchemes("MyAuth") == HTTPBearer)
    assert(!endpoint.parameters.exists(i => i.in == "header" && i.name == "Authorization")) //filtered out auth param from definition
    assert(endpoint.authentication.get == "MyAuth")

  }


}
