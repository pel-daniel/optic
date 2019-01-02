package com.opticdev.server.http.routes.socket.agents

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import better.files.File
import com.opticdev.arrow.changes.ChangeGroup
import com.opticdev.common.{SchemaRef, VersionlessSchemaRef}
import com.opticdev.sdk.descriptions.transformation.TransformationRef
import com.opticdev.server.http.routes.socket._
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import com.opticdev.server.http.routes.socket.agents.Protocol._
import com.opticdev.server.http.routes.socket.editors.EditorConnection._
import com.opticdev.server.state.ProjectsManager

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

class AgentConnection(projectDirectory: String, actorSystem: ActorSystem)(implicit projectsManager: ProjectsManager) extends Connection {

  private[this] val connectionActor = actorSystem.actorOf(Props(classOf[AgentConnectionActor], projectDirectory, projectsManager))

  def chatInSink(sender: String) = Sink.actorRef[AgentEvents](connectionActor, Terminated)

  def sendUpdate(event: UpdateAgentEvent) = {
    connectionActor ! event
  }

  override def websocketFlow = {
    val in =
      Flow[String]
        .map(i=> {
          val parsedTry = Try(Json.parse(i).as[JsObject])
          val eventTry  = Try(parsedTry.get.value("event").as[JsString].value)
          val message = if (eventTry.isSuccess) {

            //@todo replace Unknown event with invalid event response
            eventTry.get match {
              case "put-update" => {
                val idTry = Try( (parsedTry.get \ "id").get.as[JsString].value )
                val editorSlugOption = Try( (parsedTry.get \ "editorSlug").get.as[JsString].value ).toOption
                val newValueTry = Try( (parsedTry.get \ "newValue").get.as[JsObject] )
                if (idTry.isSuccess && newValueTry.isSuccess) {
                  PutUpdate(idTry.get, newValueTry.get, editorSlugOption)
                } else UnknownEvent(i)
              }

              case "post-changes" => {
                import com.opticdev.arrow.changes.JsonImplicits._
                val editorSlugOption = Try( (parsedTry.get \ "editorSlug").get.as[JsString].value ).toOption
                val changes = Try(Json.fromJson[ChangeGroup]((parsedTry.get \ "changes").get).get)
                if (changes.isSuccess) {
                  PostChanges(changes.get, editorSlugOption)
                } else {
                  UnknownEvent(i)
                }
              }

              case "transformation-options" => {
                val transformationRef = Try( TransformationRef.fromString( (parsedTry.get \ "transformationRef").get.as[JsString].value) ).flatten
                if (transformationRef.isSuccess) {
                  TransformationOptions(transformationRef.get)
                } else {
                  UnknownEvent(i)
                }

              }

              case "get-sync-patch" => {
                val editorSlugTry = Try( (parsedTry.get \ "editorSlug").get.as[JsString].value )
                if (editorSlugTry.isSuccess) {
                  StageSync(editorSlugTry.get)
                } else {
                  UnknownEvent(i)
                }
              }

              case "collect-all" => {
                val schema = Try( (parsedTry.get \ "schema").get.as[JsString].value )
                val versionlessSchema = schema.map(VersionlessSchemaRef)
                if (versionlessSchema.isSuccess) {
                  CollectAll(versionlessSchema.get)
                } else {
                  UnknownEvent(i)
                }
              }
              case "start-runtime-analysis" => StartRuntimeAnalysis()
              case "finish-runtime-analysis" => FinishRuntimeAnalysis()

              case "prepare-snapshot" => PrepareSnapshot()

              //does not receive anything from agent...yet
              case _ => UnknownEvent(i)
            }
          } else {
            if (i == "ping") {
              Pong()
            } else {
              UnknownEvent(i)
            }
          }

          message
        })
        .to(chatInSink(projectDirectory))

    val out =
      Source.actorRef[OpticEvent](1, OverflowStrategy.fail)
        .mapMaterializedValue(connectionActor ! Registered(_))

    Flow.fromSinkAndSource(in, out)
  }

  def poison = connectionActor ! PoisonPill

}

object AgentConnection extends ConnectionManager[AgentConnection, AgentSocketRouteOptions] {
  override def apply(projectDirectory: String, socketRouteOptions: AgentSocketRouteOptions)(implicit actorSystem: ActorSystem, projectsManager: ProjectsManager) = {
    println("Agent connected for: "+ projectDirectory)
    new AgentConnection(projectDirectory, actorSystem)
  }

  //only sends to CLI clients that are monitoring the correct project
  def broadcastUpdate(update: UpdateAgentEvent) = {
    val listening = listConnections.get(update.projectDirectory)
    listening.foreach(_.sendUpdate(update))
  }

  def killAgent(slug: String) = {
    val connectionOption = connections.get(slug)
    if (connectionOption.isDefined) {
      connectionOption.get.poison
      connections -= slug
    }
  }
}
