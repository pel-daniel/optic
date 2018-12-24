package com.opticdev.common.spec_types

import com.opticdev.common.api.{Level, SpecError, SpecIssue, SpecWarning}

trait ApiIssue extends SpecIssue

//Endpoint
case class NoResponses(identifier: String) extends ApiIssue {
  override val message: String = "REST endpoints must have >= 1 response"
  override val resolutions: Vector[Resolution] = Vector()
  override val level: Level = SpecError
}

//Request Body
case class RequestBodyWithoutSchema(identifier: String) extends ApiIssue {
  override val message: String = "Request Body schema is ambiguous."
  override val resolutions: Vector[Resolution] = Vector()
  override val level: Level = SpecWarning
}

case class RequestBodyWithoutContentType(identifier: String) extends ApiIssue {
  override val message: String = "Request Body schema is defined, but without a content-type."
  override val resolutions: Vector[Resolution] = Vector()
  override val level: Level = SpecWarning
}

//Response Body
case class ResponseBodyWithoutSchema(identifier: String) extends ApiIssue {
  override val message: String = "Response schema is ambiguous."
  override val resolutions: Vector[Resolution] = Vector()
  override val level: Level = SpecWarning
}

case class ResponseBodyWithoutContentType(identifier: String) extends ApiIssue {
  override val message: String = "Response Body schema is defined, but without a content-type."
  override val resolutions: Vector[Resolution] = Vector()
  override val level: Level = SpecWarning
}