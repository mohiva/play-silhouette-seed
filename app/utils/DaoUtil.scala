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
    x.map {
      case results => {
        val seq: Seq[B] = results.map(_._2).map {
          case Some(b) => Some(b)
          case _ => None.asInstanceOf[Option[B]]
        }.filterNot(_.isEmpty).map(_.get) match {
          case seq if (seq.nonEmpty) => seq
          case _ => Seq()
        }

        results.headOption.map {
          case (a, _) => (a, seq)
        }
      }
    }
  }
}
