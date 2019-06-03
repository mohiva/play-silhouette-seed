package models.daos

import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._
import models.generated.Tables.profile.api._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Concrete Dao implementation for [[SecurityRole]]
 */
@Singleton
class SecurityRoleDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[SecurityRole, SecurityRoleRow, Long](dbConfigProvider, SecurityRole) with SecurityRoleDao {

  /**
   * Returns all security roles applying for the given user.
   *
   * @param user the user to search the roles for.
   * @return all security roles applying for the given user.
   */
  override def find(user: UserRow): Future[Seq[SecurityRoleRow]] = {
    val action = (for {
      userSecurityRole <- UserSecurityRole if userSecurityRole.userId === user.id
      securityRole <- SecurityRole if securityRole.id === userSecurityRole.securityRoleId
    } yield (securityRole)).result
    db.run(action)
  }

  /**
   * Grants role to a given user.
   *
   * @param user The user to grant the security role to.
   * @param securityRole the security role.
   */
  override def grantRole(user: UserRow, securityRole: SecurityRoleRow): Future[Int] = {
    db.run(UserSecurityRole.insertOrUpdate(UserSecurityRoleRow(user.id, securityRole.id)))
  }
}