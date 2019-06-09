package models.services

import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import com.mohiva.play.silhouette.api.services.IdentityService
import models.generated.Tables.{ LoginInfoRow, SecurityRoleRow, UserRow }
import constants.SecurityRoleKeys
import providers.MySocialProfile

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[UserRow] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: Long): Future[Option[UserRow]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @param extLoginInfo The Silhouette LoginInfo instance
   * @return The saved user.
   */
  def create(user: UserRow, extLoginInfo: ExtLoginInfo): Future[UserRow]

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save that contains the required LoginInfo.
   * @return The user for whom the profile was saved.
   */
  def create(profile: MySocialProfile): Future[UserRow]

  /**
   * Returns the LoginInfo that corresponds to the user.
   *
   * @return the LoginInfo that corresponds to the user.
   */
  def loginInfo(user: UserRow): Future[Option[LoginInfoRow]]

  /**
   * Returns a sequence of roles this user has.
   *
   * @return a sequence of roles this user has.
   */
  def roles(user: UserRow): Future[Seq[SecurityRoleRow]]

  /**
   * Returns the updated user.
   *
   * @param user the user to update
   * @return the updated user.
   */
  def update(user: UserRow): Future[UserRow]
}

/**
 * Companion object for the UserService interface
 */
object UserService {
  /**
   * Provides implicit extensions to the UserRow e.g. looking up and attaching the
   * corresponding LoginInfo
   *
   * @param user the [[UserRow]] instance
   * @param userService the implicit `UserService` instance.
   * @param ec the implicit `ExecutionContext` instance.
   */
  implicit class withExtensions(user: UserRow)(implicit userService: UserService, ec: ExecutionContext) {
    def loginInfo: Future[Option[ExtLoginInfo]] = {
      userService.loginInfo(user).map(_.map { row =>
        ExtLoginInfo(row.providerId, row.providerKey)
      })
    }

    def roles: Future[Seq[SecurityRoleKeys.Type]] = {
      userService.roles(user).map(_.map { row =>
        SecurityRoleKeys.withName(row.name)
      })
    }

    def hasRole(role: SecurityRoleKeys.Type): Future[Boolean] = {
      userService.roles(user).map(_.filter { row =>
        row.name == role.toString
      }.size == 1
      )
    }
  }
}