package com.useoptic.proxy.collection.body

import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.{HttpEntity, RequestEntity, FormData => URLEncodedFD}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, Materializer}
import com.useoptic.common.spec_types.RequestBody
import com.useoptic.common.spec_types.reporting.{EndpointIssue, UnableToParseBody}
import com.useoptic.proxy.collection.jsonschema.JsonSchemaBuilderUtil
import play.api.libs.json._
import com.useoptic.proxy.Lifecycle._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try
object BodyParser {

  private implicit val materializer: Materializer = ActorMaterializer()

  def parse(entity: HttpEntity)(implicit endpointErrorAccumulator: EndpointIssue => Unit): Option[RequestBody] = Try {

    val contentType = entity.contentType.mediaType.value

    contentType match {
      case "text/plain" => RequestBody(contentType, Some(JsonSchemaBuilderUtil.basicSchema("string")))
      case "text/html" => RequestBody(contentType, Some(JsonSchemaBuilderUtil.basicSchema("string")))
      case "application/json" => {
        val json = Await.result(Unmarshal(entity).to[JsValue], 10 seconds)
        RequestBody(contentType, Some(SchemaInference.infer(json)))
      }
      case "application/x-www-form-urlencoded" => {
        val form =  Await.result(Unmarshal(entity).to[URLEncodedFD], 10 seconds)
        val fields = form.fields.map{ case (key, value) => key -> {
          val tryNumber = Try(JsNumber(BigDecimal.apply(value)))
          val tryBoolean = Try(JsBoolean(value.toBoolean))
          if (tryNumber.isSuccess) {
            tryNumber.get
          } else if (tryBoolean.isSuccess) {
            tryBoolean.get
          } else {
            JsString(value)
          }
        }}

        RequestBody(contentType, Some(SchemaInference.infer(JsObject(fields))))
      }
      case "multipart/form-data" => {
        endpointErrorAccumulator(UnableToParseBody(contentType))
        throw new UnsupportedContentType(contentType)
      }
      case "application/xml" => {
        endpointErrorAccumulator(UnableToParseBody(contentType))
        throw new UnsupportedContentType(contentType)
      }

      case _ => RequestBody(contentType, None)
    }
  }.toOption


  def mergeBody(bodies: (Option[String], Option[JsObject]) *): (Option[String], Option[JsObject]) = {

    val enforcedContentType = bodies.find(_._1.isDefined).flatMap(_._1)

    if (enforcedContentType.isEmpty) { //can't merge if we don't know the types
      return (None, None)
    }

    val schemasToMerge = bodies.collect {
      //@assumption Content-Type of first observed is the only valid option for this response
      case (contentType, schema) if schema.isDefined && contentType == enforcedContentType => schema.get
    }.distinct

    if (schemasToMerge.size == 1) {
      (enforcedContentType, Some(schemasToMerge.head))
    } else {
      (enforcedContentType, Some(JsonSchemaBuilderUtil.oneOfBase(schemasToMerge:_*)))
    }

  }

}


//Exceptions
case class UnsupportedContentType(contentType: String) extends Exception {
  override def getMessage: String = s"Unsupported Content Type "+contentType
}

case class InvalidBodyContents(expected: String) extends Exception {
  override def getMessage: String = s"The body for this request is not valid "+expected
}