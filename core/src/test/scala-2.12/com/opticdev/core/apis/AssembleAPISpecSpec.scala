package com.opticdev.core.apis

import com.opticdev.common.{PackageRef, SchemaRef}
import com.opticdev.core.sourcegear.annotations.NameAnnotation
import com.opticdev.core.sourcegear.graph.model.{FlatModelNode, ModelAnnotations, ModelNode}
import com.opticdev.core.sourcegear.snapshot.Snapshot
import org.scalatest.FunSpec
import play.api.libs.json.{JsObject, Json}

class AssembleAPISpecSpec extends FunSpec {
  val apiSchemaId = SchemaRef(Some(PackageRef("apiatlas:schemas")), "endpoint")
  def snapshotFixture(endpoints: Vector[(String, JsObject)]) = {

    val endpointMap = endpoints.map(i=> {
      val mn = ModelNode(apiSchemaId, JsObject.empty, JsObject.empty, null, 1, Map(), i._1, false)(null)
      mn.attachAnnotations(ModelAnnotations(Some(NameAnnotation(i._1, null, false)), None, None, Vector()))
      (mn.asInstanceOf[FlatModelNode], i._2)
    }).toMap

    Snapshot(null, null, Map(), endpointMap, Map(), Map(), Vector())
  }

  val exampleAPI = Vector(
    ("Hello", Json.parse(
      """
        |{
        |	"method": "post",
        |	"url": "/my-url",
        |	"parameters": [{"in": "query", "name": "test"}]
        |}
      """.stripMargin).as[JsObject])
  )

  it("can collect endpoints into spec") {
    val snapshot = snapshotFixture(exampleAPI)
    val result = AssembleAPISpec.fromSnapshot(snapshot)
    assert(result.result.endpoints.size == 1)
  }
}
