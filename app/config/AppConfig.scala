package config

import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import akka.util.Timeout
import com.google.inject.Inject
import play.api.Configuration
import play.api.libs.concurrent.Execution

import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject() (configuration: Configuration){

  implicit val defaultThreadPool = Execution.Implicits.defaultContext

  implicit val requestTimeout = Timeout(getDuration("app.request.timeout"))

  val maxNumberOfWorkers = getConfig("app.workers.max").toInt

  val phantomJs = getConfig("app.phantomjs")

  val cacheDirectory = getConfig("app.cache.dir")

  val cacheEvictionPeriod = getDuration("app.cache.evictionPeriod")

  val deleteCacheOnShutdown = getBoolean("app.cache.deleteOnShutdown") 


  // -- private helpers -------------------------------------------------------

  private def getConfig(key: String) =
    configuration.getString(key).getOrElse(
      sys.error(s"Please assign the '$key' property in application.conf"))

  private def getBoolean(key: String) =
    configuration.getBoolean(key).getOrElse(
      sys.error(s"Please assign the '$key' property in application.conf"))

  private def getDuration(key: String) =
    configuration.getMilliseconds(key)
      .map(millis => FiniteDuration(millis, TimeUnit.MILLISECONDS))
      .getOrElse(sys.error(s"Please assign the '$key' property in application.conf"))
}