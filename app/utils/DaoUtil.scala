package utils

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

/**
 * Utility methods shared across multiple Dao implementations
 */
trait DaoUtil {
  /**
   * Returns the simplified structure of a [[slick.lifted.TableQuery]] result
   * @param x the resulting expression.
   * @param ec the [[ExecutionContext]] instance in scope.
   * @tparam A The master Table row parameter type.
   * @tparam B The detail Table row parameter type.
   * @return the simplified structure of a [[slick.lifted.TableQuery]] result
   */
  implicit def simplify[A, B](x: Future[Seq[(A, Option[B])]])(implicit ec: ExecutionContext): Future[Option[(A, Seq[B])]] = {
    x.map(_.groupBy(_._1).mapValues(_.flatMap(_._2)).toSeq.headOption)
  }
}
