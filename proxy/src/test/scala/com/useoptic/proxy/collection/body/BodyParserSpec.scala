package com.useoptic.proxy.collection.body

import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.Multipart.General.{BodyPart, Strict}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, FormData => URLEncodedFD}
import com.useoptic.common.spec_types.reporting.EndpointIssue
import com.useoptic.proxy.collection.{ErrorAccumulator, TestData}
import org.scalatest.FunSpec
import play.api.libs.json.Json

class BodyParserSpec extends FunSpec {

  it("text/plain handler") {
    val result = BodyParser.parse(HttpEntity("Basic String"))(new ErrorAccumulator().add("a"))
    assert(result.get.contentType == "text/plain")
    assert(result.get.schema.get.toString() == """{"$schema":"http://json-schema.org/draft-04/schema#","type":"string"}""")
  }

  it("application/json handler") {
    val result = BodyParser.parse(HttpEntity(ContentTypes.`application/json`, Json.obj("Hello" -> true).toString()))(new ErrorAccumulator().add("a"))
    assert(result.get.contentType == "application/json")
    assert(result.get.schema.get.toString() == """{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"Hello":{"type":"boolean"}}}""")
  }

  it("application/x-www-form-urlencoded handler") {
    val result = BodyParser.parse(URLEncodedFD("hello" -> "there", "me" -> "true").toEntity)(new ErrorAccumulator().add("a"))
    assert(result.get.contentType == "application/x-www-form-urlencoded")
    assert(result.get.schema.get.toString() == """{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"hello":{"type":"string"},"me":{"type":"boolean"}}}""")
  }

}