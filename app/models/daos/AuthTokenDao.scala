package models.daos

import java.util.UUID
import models.daos.generic.GenericDao
import models.generated.Tables._
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Give access to the [[AuthToken]] object.
 */
trait AuthTokenDao extends GenericDao[AuthToken, AuthTokenRow, Long] {

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthTokenRow]]

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: DateTime): Future[Seq[AuthTokenRow]]

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Int]
}
