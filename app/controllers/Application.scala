package controllers

import javax.inject.{Named, Inject, Singleton}

import actors.{ScreenShotActor, TooManyRequestsException}
import actors.msgs.WebPage
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.StreamConverters
import config.AppConfig
import play.api.http.HttpEntity
import play.api.mvc._
import util.InputSource

import scala.concurrent.Future

@Singleton
class Application @Inject() (
  config: AppConfig,
  @Named(ScreenShotActor.name) screenShotActor: ActorRef) extends Controller {

  import config.{defaultThreadPool, requestTimeout}

  def capture(url: String) = Action.async { streamFile(askForScreenShotOf(url)) }


  // -- private helpers -------------------------------------------------------

  private def askForScreenShotOf(url: String) = {
    (screenShotActor ? WebPage(url)).mapTo[InputSource]
  }

  private def streamFile(screenshot: Future[InputSource]) = {
    screenshot map { in =>
      val stream = StreamConverters.fromInputStream(in.getInputStream)
      val entity = HttpEntity.Streamed(stream, None, Some("image/png"))
      Ok.sendEntity(entity)

    } recover {
      case e: TooManyRequestsException =>
        ServiceUnavailable("The system is currently overloaded. Try again later.")
    }
  }
}