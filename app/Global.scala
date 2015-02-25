
import play.api.{ GlobalSettings, Application }
import java.io.IOException
import util.{ AppConfig, PhantomJs, UnsafeFileCache }

object Global extends GlobalSettings {
  
  override def beforeStart(app: Application): Unit = {
    verifyConfig()
    verifyPhantomJsIsInstalled()
    UnsafeFileCache.init()
  }
  
  override def onStop(app: Application): Unit = {
    purgeFileCacheIfNeeded()
  }


  // -- private helper methods ------------------------------------------------

  private def verifyConfig() = {
    AppConfig.requestTimeout
  }

  private def verifyPhantomJsIsInstalled() = {
    try PhantomJs.getVersion()
    catch { case e: IOException => sys.error("phantomjs not installed!") }
  }

  private def purgeFileCacheIfNeeded() = {
    if (AppConfig.deleteCacheOnShutdown) {
      UnsafeFileCache.evictAllEntries()
    }
  }
}