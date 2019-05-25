package utils

import play.api.db.slick._
import slick.dbio.{ DBIOAction, NoStream }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

/**
 * Blocks until the future is done, implicitly
 */
trait AwaitUtil {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
   * Returns the result of executing the action and retrieving the Future result
   * @param action The action to be executed
   * @param dbConfigProvider The db to run under implicit parameter
   * @tparam E Concrete Entity result type
   * @return the result of executing the action and retrieving the Future result
   */
  implicit def await[E](action: DBIOAction[E, NoStream, _])(implicit dbConfigProvider: DatabaseConfigProvider) =
    Await.result(dbConfigProvider.get.db.run(action), Duration.Inf)

  //------------------------------------------------------------------------
  /**
   * Returns the result of executing and waiting the given Future
   * @param f Future to execute and wait for
   * @tparam E concrete Entity result type
   * @return the result of executing and waiting the given Future
   */
  implicit def await[E](f: Future[E]) = Await.result(f, Duration.Inf)
}