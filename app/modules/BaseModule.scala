package modules

import com.google.inject.AbstractModule
import models.daos.{ AuthTokenDao, AuthTokenDaoImpl }
import models.services.{ AuthTokenService, AuthTokenServiceImpl }
import net.codingwell.scalaguice.ScalaModule

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {
  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[AuthTokenDao].to[AuthTokenDaoImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
  }
}
