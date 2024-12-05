package core

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{HttpMessage, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, onComplete, path, pathPrefix}
import akka.http.scaladsl.server.{Directives, Route}
import core.Server.SYSTEM

import scala.util.{Failure, Success}

class TestLayer extends Layer[HttpMessage, HttpMessage, HttpRequest, HttpResponse]("test_actor", "test") {

  override def createRoutes(directivePath: String): Route =
    SYSTEM.log.info("Handling message")
    SYSTEM.log.info("Handling message: test")
    path(directivePath) {
      SYSTEM.log.info("Handling message: test :" + directivePath)
      get {
        entity(as[HttpRequest]) { request =>
          onComplete(handle(request)) {
            case Success(response) =>
              complete(response.response)
            case Failure(exception) => complete(InternalServerError, s"An error occurred ${exception.getMessage}")
          }
        }
      }
    }

  override def handleMessage(request: HttpRequest): MessageOut[HttpMessage, HttpResponse] = {
    SYSTEM.log.info("Handling message:" + request)
    MessageOut(
      HttpResponse.apply()
    )
  }

}
