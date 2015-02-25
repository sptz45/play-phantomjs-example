package actors

import akka.actor.ActorRef

package msgs {
  case class WebPage(url: String)
  case class ScreenShotRequest(url: String, requestor: ActorRef)
  case class ScreenShotReply(url: String, screenshot: Array[Byte])
  case object PeriodicCacheEviction
}