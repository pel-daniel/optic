package com.opticdev.common.spec_types

import com.opticdev.common.api.{Level, SpecError, SpecWarning}
import play.api.libs.json.Json.format
import play.api.libs.json.{Json, _}
import io.leonard.TraitFormat.{caseObjectFormat, traitFormat}
import play.api.libs.json.Json.format
import play.api.libs.json._

import scala.util.Try

object SpecJSONSerialization {

  implicit val rangeJsonFormats = new Format[Range] {
    override def reads(json: JsValue): JsResult[Range] = {
      JsSuccess(Range(
        (json.as[JsObject] \ "start").get.as[JsNumber].value.toInt,
        (json.as[JsObject] \ "end").get.as[JsNumber].value.toInt
      ))
    }
    override def writes(o: Range): JsValue = {
      JsObject(Seq(
        "start" -> JsNumber(o.start),
        "end" -> JsNumber(o.end)
      ))
    }
  }

  //Api Spec
  implicit val apiLevelFormats = traitFormat[Level] << caseObjectFormat(SpecError) << caseObjectFormat(SpecWarning)
  implicit val apiIssueFormats = traitFormat[ApiIssue] << format[NoResponses] << format[RequestBodyWithoutSchema] << format[RequestBodyWithoutContentType] << format[ResponseBodyWithoutSchema] << format[ResponseBodyWithoutContentType]
  implicit val projectIssueFormats = traitFormat[ProjectIssue] << format[NoTestCommand] << format[IncompleteTestCoverage]
  implicit val opticAPIParameterFormats = Json.format[Parameter]
  implicit val opticAPIResponseFormats = Json.format[Response]
  implicit val opticAPIRequestBodyFormats = Json.format[RequestBody]
  implicit val opticAPIEndpointFormats = Json.format[Endpoint]
  implicit val opticAPIServersFormats = Json.format[Servers]
  implicit val opticAPIDescriptionFormats = Json.format[APIDescription]
  implicit val opticAPISpecFormats = Json.format[OpticAPISpec]
  implicit val opticProjectSnapshotFormats = Json.format[OpticProjectSnapshot]

}
