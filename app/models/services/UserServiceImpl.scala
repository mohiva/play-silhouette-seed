package models.services

import javax.inject.Inject
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.daos.{ LoginInfoDao, UserDao }
import models.generated.Tables
import models.generated.Tables.{ LoginInfoRow, UserRow }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 *
 * @param userDao The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject() (userDao: UserDao, loginInfoDao: LoginInfoDao)(implicit ex: ExecutionContext) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  override def retrieve(id: Long): Future[Option[UserRow]] = userDao.findById(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param extLoginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  override def retrieve(extLoginInfo: ExtLoginInfo): Future[Option[UserRow]] = {
    userDao.find(extLoginInfo)
  }

  /**
   * Creates a new user.
   *
   * @param user The user to save.
   * @param extLoginInfo The Silhouette LoginInfo instance
   * @return The saved user.
   */
  override def create(user: UserRow, extLoginInfo: ExtLoginInfo): Future[UserRow] = userDao.create(user, extLoginInfo)

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  override def create(profile: CommonSocialProfile): Future[UserRow] = {
    userDao.find(profile.loginInfo).flatMap {
      case Some(user) => { // update user with profile
        val updated = user.copy(
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email,
          avatarUrl = profile.avatarURL
        )
        userDao.update(updated).map(affected => if (affected == 1) updated else None.asInstanceOf[UserRow])
      }
      case None => // insert a new user
        userDao.create(UserRow(
          0L,
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email,
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
  override def loginInfo(user: Tables.UserRow): Future[Option[LoginInfoRow]] = {
    loginInfoDao.findById(user.id)
  }

  /**
   * Returns the number of affected rows, one if succeeded, zero otherwise.
   *
   * @param user the user to update
   * @return the number of affected rows, one if succeeded, zero otherwise.
   */
  override def update(user: UserRow): Future[UserRow] = {
    userDao.update(user).map(affected => if (affected == 1) user else None.asInstanceOf[UserRow])
  }
}
