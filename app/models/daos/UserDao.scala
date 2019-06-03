package models.daos

import models.generated.Tables._
import models.daos.generic.GenericDaoAutoInc
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDao extends GenericDaoAutoInc[User, UserRow, Long] {
  /**
   * Finds an user by its loginInfo.
   *
   * @param extLoginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(extLoginInfo: ExtLoginInfo): Future[Option[UserRow]]

  /**
   * Returns the newly created user. Creates an user including and links
   * her to the given loginInfo.
   *
   * @param user The user to save.
   * @param extLoginInfo the loginInfo to save with the user.
   * @return the newly created user with updated id.
   */
  def create(user: UserRow, extLoginInfo: ExtLoginInfo): Future[UserRow]
}
