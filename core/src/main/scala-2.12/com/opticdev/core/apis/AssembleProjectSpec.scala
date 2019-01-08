package com.opticdev.core.apis

import com.opticdev.common.spec_types.{APIDescription, Endpoint, OpticAPISpec, OpticProjectSnapshot}
import com.opticdev.core.sourcegear.snapshot.Snapshot
import play.api.libs.json.JsError


object AssembleProjectSpec {
  case class ProjectSpecGenerationResult(result: OpticProjectSnapshot, errors: Vector[String])

  def fromSnapshot(snapshot: Snapshot, testcmd: Option[String], projectName: String): ProjectSpecGenerationResult = {

    val endpoints = snapshot
      .expandedValues
      .filter(i => i._1.schemaId.internalFull == "apiatlas:schemas/endpoint" && {
        val value = snapshot.expandedValues(i._1)
        snapshot.sourceGear.findSchema(i._1.schemaId).exists(_.validate(value))
      })
      .map{case (modelNode, value) => Endpoint.fromJson(value, modelNode.annotations.name.map(_.name))}

    val apiSpec = OpticAPISpec(APIDescription.empty, endpoints.collect{case i if i.isSuccess => i.get}.toVector)

    val projectSnapshot = OpticProjectSnapshot(apiSpec, Vector(), Vector(), projectName)

    ProjectSpecGenerationResult(
      projectSnapshot,
      endpoints.collect{case i if i.isError=> i.asInstanceOf[JsError].toString}.toVector
    )
  }

}
