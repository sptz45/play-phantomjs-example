package controllers

import scala.concurrent.Future
import akka.pattern.ask
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import actors.{ ScreenShotActor, TooManyRequestsException }
import actors.msgs.WebPage
import util.AppConfig.{ defaultThreadPool, requestTimeout }
import util.InputSource

object Application extends Controller {

  private val screenShotActor = Akka.system.actorOf(ScreenShotActor.props, "screenshot")

  def capture(url: String) = Action.async { stream(askForScreenShotOf(url)) }


  // -- private helpers -------------------------------------------------------

  private def askForScreenShotOf(url: String) = {
    (screenShotActor ? WebPage(url)).mapTo[InputSource]
  }

  private def stream(screenshot: Future[InputSource]) = {
    screenshot map { in =>
      Result(
        header = ResponseHeader(200),
        body = Enumerator.fromStream(in.getInputStream))
    } recover {
      case e: TooManyRequestsException =>
        ServiceUnavailable("The system is currently overloaded. Try again later.")
    }
  }
}