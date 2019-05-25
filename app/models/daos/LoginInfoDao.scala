package models.daos

import java.util.UUID

import models.daos.generic.GenericDao
import models.generated.Tables._

import scala.concurrent.Future

trait LoginInfoDao extends GenericDao[LoginInfo, LoginInfoRow, Long] {
  /**
   * Returns the `LoginInfo` found by provider id and key, None otherwise.
   *
   * @param providerId The provider id
   * @param providerKey the provider key
   * @return some loginInfo if exists, None otherwise
   */
  def find(providerId: UUID, providerKey: UUID): Future[Option[LoginInfoRow]]
}