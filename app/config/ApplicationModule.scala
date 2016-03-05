package config

import actors.ScreenShotActor
import com.google.inject.{AbstractModule, Provides}
import play.api.libs.concurrent.AkkaGuiceSupport
import util.{PhantomJs, UnsafeFileCache}


class ApplicationModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[ApplicationLifecycleListener]).asEagerSingleton()
    bindActor[ScreenShotActor](ScreenShotActor.name)
  }


  @Provides
  def cacheProvider(config: AppConfig): UnsafeFileCache = new UnsafeFileCache(config.cacheDirectory)

  @Provides
  def phantomJsProvider(config: AppConfig): PhantomJs = new PhantomJs(config.phantomJs)
}
