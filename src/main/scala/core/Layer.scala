package core

import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.marshalling.{PredefinedToEntityMarshallers, ToEntityMarshaller}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.Timeout
import core.Server.SYSTEM
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.concurrent.Future

final case class MessageIn[A, B, C <: A, D <: B](request: C, replyTo: ActorRef[MessageOut[B, D]])

final case class MessageOut[G, E <: G](response: E)

abstract class Layer[A, B, C <: A, D <: B](name: String, directivePath: String) extends CORSHandler {
  
  implicit val timeout: Timeout = Timeout.create(SYSTEM.settings.config.getDuration("my-app.routes.ask-timeout"))

  private var systemActor: ActorRef[MessageIn[A, B, C, D]] = null

  def handleMessage(request: C): MessageOut[B, D]

  def createRoutes(directivePath: String) : Route

  def apply(): Behavior[MessageIn[A, B, C, D]] = handle()

  protected def handle(message: C): Future[MessageOut[B, D]] = {
    systemActor.ask(MessageIn(message, _))
  }

  protected def handle(): Behavior[MessageIn[A, B, C, D]] = {
    Behaviors.receiveMessage {

      case MessageIn(request, replyTo) =>
        replyTo ! handleMessage(request)
        Behaviors.same

    }
  }

  def getRoutes(systemActor: ActorRef[MessageIn[A, B, C, D]]): Route = {
    this.systemActor = systemActor
    corsHandler(
      createRoutes(directivePath)
    )
  }

  def getName: String = {
    name
  }
  
}
