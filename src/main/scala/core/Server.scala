package core

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{pathPrefix, concat}
import akka.http.scaladsl.server.{Directives, Route}

import scala.util.{Failure, Success}

object Server {
  
  implicit var SYSTEM: ActorSystem[_] = null
  
  private def startHttpServer(host: String, port: Int, route: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding = Http().newServerAt(host, port).bind(route)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        SYSTEM.log.info(s"Server online at http://{}:{}", address.getHostString, address.getPort)
      case Failure(ex) =>
        SYSTEM.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        SYSTEM.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      SYSTEM = context.system

      val dataLay = new DataLayer()
      val contextDataLayer = context.spawn(dataLay.apply(), dataLay.getName)
      context.watch(contextDataLayer)

      val webLay = new TestLayer();
      val contextWebLayer = context.spawn(webLay.apply(), webLay.getName)
      context.watch(contextWebLayer)

      val dataRoutes = dataLay.getRoutes(contextDataLayer)
      val webRoutes = webLay.getRoutes(contextWebLayer)

      val route = pathPrefix("web") {
        concat(dataRoutes, webRoutes)
      }

      startHttpServer("localhost", 8080, route)(SYSTEM)

      Behaviors.empty
    }
    
    val system = ActorSystem[Nothing](rootBehavior, "Hydra_Server")
  }
  
}
