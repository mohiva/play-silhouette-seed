package models.daos

import org.specs2.mock.Mockito
import play.api.Application
import utils.AwaitUtil
import play.api.test.{ PlaySpecification, WithApplication }

/**
 * Base trait for all tests that need dao access via Specs2
 */
trait BaseDaoSpec extends PlaySpecification with Mockito {
  /**
   * Returns Dao context instance containing accessible daos.
   * @param app The application instance in context.
   * @return Dao context instance containing accessible daos.
   */
  protected def daoContext(implicit app: Application) = {
    Application.instanceCache[DaoContext].apply(app)
  }

  trait BaseContext extends WithApplication with AwaitUtil
}
