package com.opticdev.server.http

import com.opticdev.common.SchemaRef
import com.opticdev.core.sourcegear.graph.objects.ObjectNode
import com.opticdev.server.http.routes.SDKBridgeProtocol.TransformationTest
import play.api.libs.json.{JsObject, JsValue, Json}

package object routes {

  object SDKBridgeProtocol {
    case class TransformationTest(packageJson: JsObject, transformationId: String, input: JsObject, answers: JsObject)
    implicit val transformationTestFormat = Json.format[TransformationTest]
  }

  object RuntimeProtocol {
    case class AddObject(projectName: String, name: String, value: JsValue, abstractionOption: Option[SchemaRef]) {
      def toObjectNode: ObjectNode = ObjectNode(name, abstractionOption, value, fromRuntime = true)
    }
    case class ClearRuntimeObjects(projectName: String)

    implicit val addObjectFormat = Json.format[AddObject]
    implicit val clearRuntimeFormat = Json.format[ClearRuntimeObjects]
  }

}
