package models.services

import scala.concurrent.Future
import scala.language.postfixOps

import com.mohiva.play.silhouette.api.util.{ PasswordInfo => ExtPasswordInfo }

/**
 * Handles actions for TOTP scratch codes.
 */
trait ScratchCodeService {
  /**
   * Returns the number of deleted rows, one if successful, zero otherwise.
   * Finds the ScratchCode by userId, hash and password.
   * @param userId the user id.
   * @param target the scratch code to delete.
   * @return the number of deleted rows, one if successful, zero otherwise.
   */
  def delete(userId: Long, target: ExtPasswordInfo): Future[Int]
}
