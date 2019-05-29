package models.services

import javax.inject.Inject
import models.daos.LoginInfoDao

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }

/**
 * Handles actions for login info.
 *
 * @param loginInfoDao The auth token DAO implementation.
 * @param ec The execution context.
 */
class LoginInfoServiceImpl @Inject() (
  loginInfoDao: LoginInfoDao
)(
  implicit
  ec: ExecutionContext
) extends LoginInfoService {

  /**
   * Returns the number of inserted rows, one if successful, zero otherwise.
   * Creates a new `LoginInfo` row based on the Silhouette `LoginInfo` instance.
   *
   * @param userId the user id.
   * @param extLoginInfo the Silhouette `LoginInfo` instance.
   * @return the number of inserted rows, one if successful, zero otherwise.
   */
  override def create(userId: Long, extLoginInfo: ExtLoginInfo): Future[Int] = loginInfoDao.create(userId, extLoginInfo)

  /**
   * Returns the number of deleted rows, one if successful, zero otherwise.
   * Deletes a `LoginInfo` row by the userId and provider id combination.
   *
   * @param userId the user id.
   * @param providerId the provider id.
   * @return the number of deleted rows, one if successful, zero otherwise.
   */
  override def delete(userId: Long, providerId: String): Future[Int] = loginInfoDao.delete(userId, providerId)
}
