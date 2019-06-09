package models.services

import javax.inject.Inject
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import models.daos._
import models.generated.Tables._
import providers.MySocialProfile

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 *
 * @param daoContext The dao context to have access to all daos.
 * @param ex The execution context.
 */
class UserServiceImpl @Inject() (
  daoContext: DaoContext
)(
  implicit
  ex: ExecutionContext
) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  override def retrieve(id: Long): Future[Option[UserRow]] = daoContext.userDao.findById(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param extLoginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  override def retrieve(extLoginInfo: ExtLoginInfo): Future[Option[UserRow]] = {
    daoContext.userDao.find(extLoginInfo)
  }

  /**
   * Creates a new user.
   *
   * @param user The user to save.
   * @param extLoginInfo The Silhouette LoginInfo instance
   * @return The saved user.
   */
  override def create(user: UserRow, extLoginInfo: ExtLoginInfo): Future[UserRow] = daoContext.userDao.create(user, extLoginInfo)

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  // TODO: extend OAuth2FacebookProvider and provide correct values
  override def create(profile: MySocialProfile): Future[UserRow] = {
    daoContext.userDao.find(profile.loginInfo).flatMap {
      case Some(user) => { // update user with profile
        val updated = user.copy(
          firstName = profile.firstName.get,
          lastName = profile.lastName.get,
          birthDate = profile.birthDate.get,
          gender = profile.gender.get,
          email = profile.email.get,
          phoneNumber = None,
          avatarUrl = profile.avatarURL
        )
        daoContext.userDao.update(updated).map(affected => if (affected == 1) updated else None.asInstanceOf[UserRow])
      }
      case None => // insert a new user
        daoContext.userDao.create(UserRow(
          0L,
          firstName = profile.firstName.get,
          lastName = profile.lastName.get,
          birthDate = profile.birthDate.get,
          gender = profile.gender.get,
          email = profile.email.get,
          phoneNumber = None,
          avatarUrl = profile.avatarURL,
          activated = true
        ), profile.loginInfo)
    }
  }

  /**
   * Returns the LoginInfo that corresponds to the user.
   *
   * @return the LoginInfo that corresponds to the user.
   */
  override def loginInfo(user: UserRow): Future[Option[LoginInfoRow]] = {
    daoContext.loginInfoDao.findById(user.id)
  }

  /**
   * Returns sequence of roles this user has.
   *
   * @return sequence of roles this user has.
   */
  override def roles(user: UserRow): Future[Seq[SecurityRoleRow]] = {
    daoContext.securityRoleDao.find(user)
  }

  /**
   * Returns the number of affected rows, one if succeeded, zero otherwise.
   *
   * @param user the user to update
   * @return the number of affected rows, one if succeeded, zero otherwise.
   */
  override def update(user: UserRow): Future[UserRow] = {
    daoContext.userDao.update(user).map(affected => if (affected == 1) user else None.asInstanceOf[UserRow])
  }
}
