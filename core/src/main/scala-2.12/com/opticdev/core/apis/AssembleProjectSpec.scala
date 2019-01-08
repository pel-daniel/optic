package com.opticdev.core.apis

import com.opticdev.common.spec_types._
import com.opticdev.core.sourcegear.snapshot.Snapshot
import play.api.libs.json.JsError


object AssembleProjectSpec {
  case class ProjectSpecGenerationResult(result: OpticProjectSnapshot, errors: Vector[String])

  def fromSnapshot(snapshot: Snapshot, testcmd: Option[String], projectName: String): ProjectSpecGenerationResult = {

    val endpoints = snapshot
      .expandedValues
      .filter(i => i._1.schemaId.internalFull == "apiatlas:schemas/endpoint" && {
        if (snapshot.sourceGear != null) {
          val value = snapshot.expandedValues(i._1)
          snapshot.sourceGear.findSchema(i._1.schemaId).exists(_.validate(value))
        } else true
      })
      .map{case (modelNode, value) => Endpoint.fromJson(value, modelNode.annotations.name.map(_.name))}

    val authentication = snapshot.objectNodes
      .find(_.schemaRef.exists(_.internalFull == "apiatlas:schemas/authentication"))
      .map(i => i.name -> Authentication.fromJson(i.value))
      .toMap

    val validAuth = authentication.collect{case i if i._2.isSuccess => i._1 -> i._2.get }
    val authIssues = authentication.collect{case i if i._2.isFailure => InvalidAuthDefinition(i._2.failed.get.getMessage, i._1) }.toVector

    val validEndpoints = endpoints
      .collect{case i if i.isSuccess => {
        val endpoint = i.get
        if (authentication.nonEmpty) {
          Authentication.applyAuthToEndpoint(validAuth.head, endpoint)
        } else endpoint
      }}
      .toVector

    val apiSpec = OpticAPISpec(APIDescription.empty, validEndpoints, validAuth)

    val projectSnapshot = OpticProjectSnapshot(
      apiSpec,
      Vector(),
      Vector() ++ authIssues,
      projectName)

    ProjectSpecGenerationResult(
      projectSnapshot,
      endpoints.collect{case i if i.isError=> i.asInstanceOf[JsError].toString}.toVector
    )
  }

}
