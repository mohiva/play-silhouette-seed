package models.daos

import org.scalatest.FunSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

/**
  * Abstract base implementation for all Dao tests
  */
abstract class AbstractDaoFunSpec extends FunSpec with GuiceOneAppPerSuite {

  protected def daoContext(implicit app: Application) = {
    Application.instanceCache[DaoContext].apply(app)
  }
}
