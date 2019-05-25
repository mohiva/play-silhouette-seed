package models.daos

import java.util.UUID
import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables
import models.generated.Tables._
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
  override def find(providerId: UUID, providerKey: UUID): Future[Option[Tables.LoginInfoRow]] = {
    ???
  }
}