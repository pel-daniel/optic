package com.opticdev.common.api_types

trait ApiIssue {
  def message: String
  def resolutions: Vector[String]
  def level: Level
  def identifier: String
}

sealed trait Level
case object APIError extends Level
case object APIWarning extends Level

//Endpoint
case class NoResponses(identifier: String) extends ApiIssue {
  override def message: String = "REST endpoints must have >= 1 response"
  override def resolutions: Vector[String] = Vector()
  override def level: Level = APIError
}

//Request Body
case class RequestBodyWithoutSchema(identifier: String) extends ApiIssue {
  override def message: String = "Request Body schema is ambiguous."
  override def resolutions: Vector[String] = Vector()
  override def level: Level = APIWarning
}

case class RequestBodyWithoutContentType(identifier: String) extends ApiIssue {
  override def message: String = "Request Body schema is defined, but without a content-type."
  override def resolutions: Vector[String] = Vector()
  override def level: Level = APIWarning
}

//Response Body
case class ResponseBodyWithoutSchema(identifier: String) extends ApiIssue {
  override def message: String = "Response schema is ambiguous."
  override def resolutions: Vector[String] = Vector()
  override def level: Level = APIWarning
}

case class ResponseBodyWithoutContentType(identifier: String) extends ApiIssue {
  override def message: String = "Response Body schema is defined, but without a content-type."
  override def resolutions: Vector[String] = Vector()
  override def level: Level = APIWarning
}