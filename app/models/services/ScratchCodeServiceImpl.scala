package models.services

import com.mohiva.play.silhouette.api.util.Clock
import javax.inject.Inject
import models.daos.ScratchCodeDao
import com.mohiva.play.silhouette.api.util.{ PasswordInfo => ExtPasswordInfo }

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

/**
 * Handles actions for TOTP scratch codes.
 *
 * @param scratchCodeDao The auth token DAO implementation.
 * @param clock The clock instance.
 * @param ec The execution context.
 */
class ScratchCodeServiceImpl @Inject() (
  scratchCodeDao: ScratchCodeDao,
  clock: Clock
)(
  implicit
  ec: ExecutionContext
) extends ScratchCodeService {
  /**
   * Returns the number of deleted rows, one if successful, zero otherwise.
   * Finds the ScratchCode by userId, hash and password.
   * @param userId the user id.
   * @param target the scratch code to delete.
   * @return the number of deleted rows, one if successful, zero otherwise.
   */
  override def delete(userId: Long, target: ExtPasswordInfo): Future[Int] = scratchCodeDao.delete(userId, target)
}
