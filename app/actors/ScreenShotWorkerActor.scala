package actors

import akka.actor._
import util.{ PhantomJs, InputSource }
import actors.msgs._

class ScreenShotWorkerActor(cacheActor: ActorRef) extends Actor {

  def receive: Receive = {
    case ScreenShotRequest(url, requestor) =>
      val screenshot = PhantomJs.getWebsiteAsImage(url)
      requestor ! InputSource(screenshot)
      cacheActor ! ScreenShotReply(url, screenshot)
      context.stop(self)
  }
}

object ScreenShotWorkerActor {
  def props(cacheActor: ActorRef): Props = Props(new ScreenShotWorkerActor(cacheActor))
}