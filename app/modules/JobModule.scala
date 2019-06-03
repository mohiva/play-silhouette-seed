package modules

import jobs.{ AuthTokenCleaner, LastLoginUpdater, Scheduler }
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 * The job module.
 */
class JobModule extends ScalaModule with AkkaGuiceSupport {
  /**
   * Configures the module.
   */
  override def configure() = {
    bindActor[AuthTokenCleaner]("auth-token-cleaner")
    bindActor[LastLoginUpdater]("lastloginupdater-actor")
    bind[Scheduler].asEagerSingleton()
  }
}
