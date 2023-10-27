package core

import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{PredefinedToEntityMarshallers, ToEntityMarshaller}
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives.{as, complete, entity, onComplete, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.Timeout
import core.Server.SYSTEM
import core.protobuf.TestRequest.TestRequest
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait Marshalling[T <: GeneratedMessage, E <: GeneratedMessage] extends DefaultJsonProtocol with SprayJsonSupport {

  implicit def protobufMarshaller: ToEntityMarshaller[E] = PredefinedToEntityMarshallers.ByteArrayMarshaller.compose[E](r => r.toByteArray)

  implicit def requestMarshaller(implicit companion: GeneratedMessageCompanion[T]): FromEntityUnmarshaller[T] = {
    Unmarshaller.byteArrayUnmarshaller.map[T](bytes => companion.parseFrom(bytes))
  }
// unmarshaller that works without wildcards
//  implicit def requestMarshaller2(implicit companion: GeneratedMessageCompanion[TestRequest]): FromEntityUnmarshaller[TestRequest] = {
//    Unmarshaller.byteArrayUnmarshaller.map[TestRequest](bytes => companion.parseFrom(bytes))
//  }

}

final case class MessageIn[T <: GeneratedMessage, E <: GeneratedMessage](request: T, replyTo: ActorRef[MessageOut[E]])

final case class MessageOut[E <: GeneratedMessage](response: E)

abstract class Layer[T <: GeneratedMessage, E <: GeneratedMessage](name: String, directivePath: String) 
  extends CORSHandler with Marshalling[T, E] {
  
  implicit val timeout: Timeout = Timeout.create(SYSTEM.settings.config.getDuration("my-app.routes.ask-timeout"))

  private var systemActor: ActorRef[MessageIn[T, E]] = null

  def handleMessage(request: T): MessageOut[E]

  private def createRoutes(): Route = {
    pathPrefix("test") {
      path(directivePath) {
        post {
          // works when using TestRequest as type and above requestUnmarshaller2
          entity(as[/*TestRequest*/T]) { request =>
            onComplete(handle(request)) {
              case Success(response) =>
                complete(response.response)
              case Failure(exception) => complete(InternalServerError, s"An error occurred ${exception.getMessage}")
            }
          }
        }
      }
    }
  }

  def apply(): Behavior[MessageIn[T, E]] = handle()

  private def handle(message: T): Future[MessageOut[E]] = {
    systemActor.ask(MessageIn(message, _))
  }

  private def handle(): Behavior[MessageIn[T, E]] = {
    Behaviors.receiveMessage {

      case MessageIn(request, replyTo) =>
        replyTo ! handleMessage(request)
        Behaviors.same

    }
  }

  def getRoutes(systemActor: ActorRef[MessageIn[T, E]]): Route = {
    this.systemActor = systemActor
    corsHandler(
      createRoutes()
    )
  }

  def getName: String = {
    name
  }
  
}
