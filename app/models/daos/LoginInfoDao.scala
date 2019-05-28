package models.daos

import models.daos.generic.GenericDao
import models.generated.Tables._
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }

import scala.concurrent.Future

trait LoginInfoDao extends GenericDao[LoginInfo, LoginInfoRow, Long] {
  /**
   * Returns the `LoginInfo` found by provider id and key, None otherwise.
   *
   * @param providerId The provider id
   * @param providerKey the provider key
   * @return some loginInfo if exists, None otherwise
   */
  def find(providerId: String, providerKey: String): Future[Option[LoginInfoRow]]

  /**
   * Returns the number of inserted rows, one if successful, zero otherwise.
   * Creates a new `LoginInfo` row based on the Silhouette `LoginInfo` instance.
   *
   * @param userId the user id.
   * @param extLoginInfo the Silhouette `LoginInfo` instance.
   * @return the number of inserted rows, one if successful, zero otherwise.
   */
  def create(userId: Long, extLoginInfo: ExtLoginInfo): Future[Int]

  /**
   * Returns the number of deleted rows, one if successful, zero otherwise.
   * Deletes a `LoginInfo` row by the userId and provider id combination.
   *
   * @param userId the user id.
   * @param providerId the provider id.
   * @return the number of deleted rows, one if successful, zero otherwise.
   */
  def delete(userId: Long, providerId: String): Future[Int]
}