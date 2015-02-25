package actors

import akka.actor._
import actors.msgs._
import util._

class ScreenShotActor extends Actor {

  private def maxWorkers = AppConfig.maxNumberOfWorkers

  private val workerManager = context.actorOf(
      WorkerManagerActor.props(ScreenShotWorkerActor.props(self), maxWorkers))

  private var scheduler: Cancellable = _
      
  override def preStart(): Unit = schedulePeriodicCacheEvictions()

  override def postStop: Unit = scheduler.cancel()

  override val receive: Receive = {

    case WebPage(url) =>
      UnsafeFileCache.getFile(url) match {
        case None =>
          workerManager ! ScreenShotRequest(url, sender)
        case Some(path) =>
          sender ! InputSource(path)
      }

    case ScreenShotReply(url, screenshot) =>
      UnsafeFileCache.putFile(url, screenshot)

    case PeriodicCacheEviction =>
      UnsafeFileCache.evictOldEntries()
  }

  private def schedulePeriodicCacheEvictions() = {
    val evictionPeriod = AppConfig.cacheEvictionPeriod
    implicit val threadPool = context.dispatcher
    scheduler = context.system.scheduler
      .schedule(evictionPeriod, evictionPeriod, self, PeriodicCacheEviction)
  }
}


object ScreenShotActor {
  def props = Props(new ScreenShotActor)
}