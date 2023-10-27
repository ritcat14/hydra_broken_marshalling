package core

import akka.actor.typed.Behavior
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Directives, Route}
import core.protobuf.TestRequest.TestRequest
import core.protobuf.TestResponse.TestResponse

import scala.util.{Failure, Success}

class DataLayer extends Layer[TestRequest, TestResponse]("data_actor", "data") {

  override def apply(): Behavior[MessageIn[TestRequest, TestResponse]] = super.apply()

  override def handleMessage(message: TestRequest): MessageOut[TestResponse] = {
    MessageOut(TestResponse())
  }

}
