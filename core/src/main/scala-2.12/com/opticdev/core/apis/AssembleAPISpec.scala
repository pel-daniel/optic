package com.opticdev.core.apis

import com.opticdev.common.api_types.{APIDescription, Endpoint, OpticAPISpec}
import com.opticdev.core.sourcegear.snapshot.Snapshot
import play.api.libs.json.JsError


object AssembleAPISpec {
  case class APISpecGenerationResult(result: OpticAPISpec, errors: Vector[String])

  def fromSnapshot(snapshot: Snapshot): APISpecGenerationResult = {
    val endpoints = snapshot
      .expandedValues
      .filter(_._1.schemaId.internalFull == "apiatlas:schemas/endpoint")
      .map{case (modelNode, value) => Endpoint.fromJson(value, modelNode.annotations.name.map(_.name))}

    APISpecGenerationResult(
      OpticAPISpec(APIDescription.empty, endpoints.collect{case i if i.isSuccess => i.get}.toVector),
      endpoints.collect{case i if i.isError=> i.asInstanceOf[JsError].toString}.toVector
    )
  }

}
