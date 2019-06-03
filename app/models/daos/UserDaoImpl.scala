package models.daos

import models.generated.Tables._
import javax.inject._
import models.daos.generic.GenericDaoAutoIncImpl
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import constants.SecurityRoleKeys

import scala.concurrent.{ ExecutionContext, Future }
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

/**
 * Gives access to the user object.
 */
@Singleton
class UserDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoAutoIncImpl[User, UserRow, Long](dbConfigProvider, User) with UserDao {

  /**
   * Finds a user by its login info.
   *
   * @param extLoginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  override def find(extLoginInfo: ExtLoginInfo): Future[Option[UserRow]] = {
    val action = (for {
      loginInfo <- LoginInfo if loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey
      user <- User if user.id === loginInfo.userId
    } yield user).result.headOption
    db.run(action)
  }

  /**
   * Returns the newly created user. Creates an user including and links
   * her to the given loginInfo.
   *
   * @param user The user to save.
   * @param extLoginInfo the loginInfo to save with the user.
   * @return the newly created user with updated id.
   */
  override def create(user: UserRow, extLoginInfo: ExtLoginInfo): Future[UserRow] = {
    val insertion = (for {
      user <- (User returning User.map(_.id) into ((row, id) => row.copy(id = id)) += user)
      _ <- (LoginInfo += LoginInfoRow(user.id, extLoginInfo.providerID, extLoginInfo.providerKey))
      // default user role
      securityRoleId <- SecurityRole.filter(_.name === SecurityRoleKeys.default.toString).result.head.map(_.id)
      _ <- (UserSecurityRole += UserSecurityRoleRow(user.id, securityRoleId))
    } yield user).transactionally

    db.run(insertion)
  }
}