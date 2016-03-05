package actors

import javax.inject.{Singleton, Inject}

import akka.actor._
import actors.msgs._
import config.AppConfig
import util._

@Singleton
class ScreenShotActor @Inject() (config: AppConfig, fileCache: UnsafeFileCache, phantomJs: PhantomJs) extends Actor {

  private def maxWorkers = config.maxNumberOfWorkers

  private val workerManager = context.actorOf(
      WorkerManagerActor.props(ScreenShotWorkerActor.props(self, phantomJs), maxWorkers))

  private var scheduler: Cancellable = _
      
  override def preStart(): Unit = schedulePeriodicCacheEvictions()

  override def postStop: Unit = scheduler.cancel()

  override val receive: Receive = {

    case WebPage(url) =>
      fileCache.getFile(url) match {
        case None =>
          workerManager ! ScreenShotRequest(url, sender)
        case Some(path) =>
          sender ! InputSource(path)
      }

    case ScreenShotReply(url, screenshot) =>
      fileCache.putFile(url, screenshot)

    case PeriodicCacheEviction =>
      fileCache.evictOldEntries()
  }

  private def schedulePeriodicCacheEvictions() = {
    val evictionPeriod = config.cacheEvictionPeriod
    implicit val threadPool = context.dispatcher
    scheduler = context.system.scheduler
      .schedule(evictionPeriod, evictionPeriod, self, PeriodicCacheEviction)
  }
}


object ScreenShotActor {
  final val name = "screenshot"
}