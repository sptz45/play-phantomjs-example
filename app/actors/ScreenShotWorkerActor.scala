package actors

import actors.msgs._
import akka.actor._
import util.{InputSource, PhantomJs}

class ScreenShotWorkerActor(cacheActor: ActorRef, phantomJs: PhantomJs) extends Actor {

  def receive: Receive = {
    case ScreenShotRequest(url, requestor) =>
      val screenshot = phantomJs.getWebsiteAsImage(url)
      requestor ! InputSource(screenshot)
      cacheActor ! ScreenShotReply(url, screenshot)
      context.stop(self)
  }
}

object ScreenShotWorkerActor {
  def props(cacheActor: ActorRef, phantomJs: PhantomJs): Props =
    Props(new ScreenShotWorkerActor(cacheActor, phantomJs))
}