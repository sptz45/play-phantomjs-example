package config

import java.io.IOException
import javax.inject.{Inject, Singleton}

import play.api.Application
import play.api.inject.ApplicationLifecycle
import util.{PhantomJs, UnsafeFileCache}

import scala.concurrent.Future

@Singleton
class ApplicationLifecycleListener @Inject()(
  config: AppConfig,
  lifecycle: ApplicationLifecycle,
  fileCache: UnsafeFileCache,
  phantomJs: PhantomJs) {

  lifecycle.addStopHook { () =>
    onStop()
  }

  @Inject
  def onStart(): Unit = {
    verifyConfig()
    verifyPhantomJsIsInstalled()
    fileCache.init()
  }

  def onStop(): Future[_] = {
    purgeFileCacheIfNeeded()
    Future.successful(Unit)
  }


  // -- private helper methods ------------------------------------------------

  private def verifyConfig() = {
    config.requestTimeout
  }

  private def verifyPhantomJsIsInstalled() = {
    try phantomJs.getVersion()
    catch { case e: IOException => sys.error("phantomjs not installed!") }
  }

  private def purgeFileCacheIfNeeded() = {
    if (config.deleteCacheOnShutdown) {
      fileCache.evictAllEntries()
    }
  }

}
