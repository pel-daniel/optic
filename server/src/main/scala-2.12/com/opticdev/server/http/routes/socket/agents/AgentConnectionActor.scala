package com.opticdev.server.http.routes.socket.agents

import akka.actor.{Actor, ActorRef, Status}
import com.opticdev.server.http.controllers.{ArrowPostChanges, ArrowQuery, PutUpdateRequest}
import com.opticdev.server.http.routes.socket.ErrorResponse
import com.opticdev.server.http.routes.socket.agents.Protocol._
import com.opticdev.server.http.routes.socket.editors.EditorConnection
import com.opticdev.server.http.routes.socket.editors.Protocol.FilesUpdated
import com.opticdev.server.state.ProjectsManager

import scala.concurrent.ExecutionContext.Implicits.global

class AgentConnectionActor(slug: String, projectsManager: ProjectsManager) extends Actor {

  private var connection : ActorRef = null

  private var name : String = slug
  private var version : String = ""

  override def receive: Receive = {
    case Registered(actorRef) => {
      println("worked correctly")
      connection = actorRef
    }
    case Terminated => {
      Status.Success(Unit)
      AgentConnection.killAgent(slug)
    }

    //message to client routing
    case UnknownEvent(raw) => {
      connection ! ErrorResponse("Invalid Request")
    }

    case contextUpdate: ContextFound => {
      connection ! contextUpdate
    }

    case searchResults: SearchResults => {
      connection ! searchResults
    }

    case search: AgentSearch => {
      ArrowQuery(search, projectsManager.lastProjectName)(projectsManager).executeToApiResponse.foreach(i=> {
        AgentConnection.broadcastUpdate( SearchResults(search.query, i.data, ignoreQueryUpdate = true) )
      })
    }

    case postChanges: PostChanges => {
      val future = new ArrowPostChanges(postChanges.projectName, postChanges.changes)(projectsManager).execute
        future.foreach(i=> {
          AgentConnection.broadcastUpdate( PostChangesResults(i.isSuccess, i.stagedFiles.keys.toSet) )
          EditorConnection.broadcastUpdate( FilesUpdated(i.stagedFiles) )
        })

      future.onComplete(i=> {
        if (i.isFailure) {
          println(i.failed.get)
        } else {
          println(i.get)
        }
      })

    }



    //client actions
    case update : PutUpdate => {
      //@todo handle error states
      new PutUpdateRequest(update.id, update.newValue)(projectsManager)
        .executeToApiResponse
    }

  }

}

