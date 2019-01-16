package com.useoptic.proxy.services.proxy

import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import com.useoptic.proxy.collection.RawRequest
import io.lemonlabs.uri.Url

object HttpRequestImplicits {
  implicit class HttpRequestHelper(httpRequest: HttpRequest) {

    def updateHost(host: String, port: Int): HttpRequest = {
      val updated = httpRequest.uri.withHost(host).withPort(port)
      httpRequest.copy(uri = updated)
    }

    def toOpticRequest(): RawRequest = {
      val method = httpRequest.method.value.toLowerCase
      val url = httpRequest.uri.toString()



//      RawRequest()
      null
    }

  }
}
