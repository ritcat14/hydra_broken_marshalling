package core

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Directives, Route}
import core.Server.SYSTEM
import core.protobuf.TestRequest.TestRequest
import core.protobuf.TestResponse.TestResponse
import scalapb.GeneratedMessage

import scala.util.{Failure, Success}

class DataLayer extends Layer[GeneratedMessage, GeneratedMessage, TestRequest, TestResponse]("data_actor", "data")
  with ProtobufMarshalling[TestRequest, TestResponse] {

  override def handleMessage(message: TestRequest): MessageOut[GeneratedMessage, TestResponse] = {
    SYSTEM.log.info("Handling message:" + message)
    MessageOut(TestResponse())
  }

  override def createRoutes(directivePath: String): Route = {
    path(directivePath) {
      post {
        entity(as[TestRequest]) { request =>
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
