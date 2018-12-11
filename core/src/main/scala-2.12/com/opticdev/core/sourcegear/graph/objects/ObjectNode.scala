
package com.opticdev.core.sourcegear.graph.objects

import com.opticdev.common.SchemaRef
import com.opticdev.core.sourcegear.graph.AstProjection
import com.opticdev.common.graph.BaseNode
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.util.hashing.MurmurHash3

case class ObjectNode(name: String, schemaRef: Option[SchemaRef], value: JsValue, fromRuntime: Boolean = false) extends AstProjection {

  override val id: String = {
    Integer.toHexString(
      MurmurHash3.stringHash(name) ^
        MurmurHash3.stringHash(schemaRef.map(_.full).getOrElse("NONE")) ^
        MurmurHash3.stringHash(fromRuntime.toString) ^
        MurmurHash3.stringHash(value.toString()))
  }

}

object ObjectNode {
  import com.opticdev.core.sourcegear.project.config.options.ConfigYamlProtocol.SchemaRefFormat
  implicit val objectNodeFormats = Json.format[ObjectNode]
}