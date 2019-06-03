package models.daos

import models.daos.generic.GenericDao
import models.generated.Tables._

import scala.concurrent.Future

/**
 * Concrete Dao definition for [[SecurityRole]]
 */
trait SecurityRoleDao extends GenericDao[SecurityRole, SecurityRoleRow, Long] {
  /**
   * Returns all security roles applying for the given user.
   *
   * @param user the user to search the roles for.
   * @return all security roles applying for the given user.
   */
  def find(user: UserRow): Future[Seq[SecurityRoleRow]]

  /**
   * Returns the number of inserted rows, one if successful, zero otherwise.
   *
   * @param user The user to grant the security role to.
   * @param securityRole the security role.
   * @return number of inserted rows, one if successful, zero otherwise.
   */
  def grantRole(user: UserRow, securityRole: SecurityRoleRow): Future[Int]
}
