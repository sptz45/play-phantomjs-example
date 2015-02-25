package util

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import akka.util.Timeout
import play.api.libs.concurrent.Execution
import play.api.Play.current

object AppConfig {

  implicit val defaultThreadPool = Execution.Implicits.defaultContext

  implicit val requestTimeout = Timeout(getDuration("app.request.timeout"))

  val maxNumberOfWorkers = getConfig("app.workers.max").toInt

  val phantomJs = getConfig("app.phantomjs")

  val cacheDirectory = getConfig("app.cache.dir")

  val cacheEvictionPeriod = getDuration("app.cache.evictionPeriod")

  val deleteCacheOnShutdown = getBoolean("app.cache.deleteOnShutdown") 


  // -- private helpers -------------------------------------------------------

  private def getConfig(key: String) =
    current.configuration.getString(key).getOrElse(
      sys.error(s"Please assign the '$key' property in application.conf"))

  private def getBoolean(key: String) =
    current.configuration.getBoolean(key).getOrElse(
      sys.error(s"Please assign the '$key' property in application.conf"))

  private def getDuration(key: String) =
    current.configuration.getMilliseconds(key)
      .map(millis => FiniteDuration(millis, TimeUnit.MILLISECONDS))
      .getOrElse(sys.error(s"Please assign the '$key' property in application.conf"))
}