package models.daos

import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }

import scala.concurrent.{ ExecutionContext, Future }
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class LoginInfoDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[LoginInfo, LoginInfoRow, Long](dbConfigProvider, LoginInfo) with LoginInfoDao {
  /**
   * Returns the `LoginInfo` found by provider id and key, None otherwise.
   *
   * @param providerId The provider id
   * @param providerKey the provider key
   * @return some loginInfo if exists, None otherwise
   */
  override def find(providerId: String, providerKey: String): Future[Option[LoginInfoRow]] = {
    db.run(LoginInfo.filter(loginInfo => loginInfo.providerId === providerId &&
      loginInfo.providerKey === providerKey).result.headOption)
  }

  /**
   * Returns the number of inserted rows, one if successful, zero otherwise.
   * Creates a new `LoginInfo` row based on the Silhouette `LoginInfo` instance.
   *
   * @param userId the user id.
   * @param extLoginInfo the Silhouette `LoginInfo` instance.
   * @return the number of inserted rows, one if successful, zero otherwise.
   */
  override def create(userId: Long, extLoginInfo: ExtLoginInfo): Future[Int] = {
    create(LoginInfoRow(userId, extLoginInfo.providerID, extLoginInfo.providerKey))
  }

  /**
   * Returns the number of deleted rows, one if successful, zero otherwise.
   * Deletes a `LoginInfo` row by the userId and provider id combination.
   *
   * @param userId the user id.
   * @param providerId the provider id.
   * @return the number of deleted rows, one if successful, zero otherwise.
   */
  override def delete(userId: Long, providerId: String): Future[Int] = {
    db.run(LoginInfo.filter(loginInfo => loginInfo.userId === userId && loginInfo.providerId === providerId).delete)
  }
}