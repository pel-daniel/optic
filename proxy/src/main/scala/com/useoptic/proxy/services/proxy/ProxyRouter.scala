package com.useoptic.proxy.services.proxy
import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.{ClientTransport, Http}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ClientConnectionSettings, ConnectionPoolSettings}
import com.useoptic.proxy.collection.{APIInteraction, CollectionSessionManager}
import com.useoptic.proxy.services.control.collection.Protocol._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import HttpRequestImplicits._
import com.useoptic.proxy.Lifecycle.actorSystem
import com.useoptic.proxy.Lifecycle.executionContext
import HttpResponseImplicits._

import scala.util.{Failure, Success}

object ProxyRouter {
  val routes: Route = {
    pathPrefix("") {
      extractRequest {request => {

        if (CollectionSessionManager.isRunning) {

          val session = CollectionSessionManager.session
          val configuration = CollectionSessionManager.session.configuration

          val updated = request.updateHost(configuration.host, configuration.port)

          onComplete(Http().singleRequest(updated)) { responseTry =>
            if (responseTry.isSuccess) {
              //collect relevant request information & log
              val opticRequest = request.toOpticRequest
              val opticResponse = responseTry.get.toOpticResponse
              session.logInteraction(APIInteraction(opticRequest, opticResponse))

              //forward server response to clients
              complete(responseTry.get)
            } else {
              complete(StatusCodes.ServiceUnavailable, s"Optic Proxy Failed. Service at ${configuration.host} ${configuration.port}")
            }
          }

        } else {
          complete(StatusCodes.ServiceUnavailable, s"Optic Proxy Not Started. Request Rejected.")
        }

      }}
    }
  }
}
