package action

import com.mohiva.play.silhouette.api._
import models.generated.Tables.UserRow
import models.services.UserService
import play.api.mvc._
import constants.SecurityRoleKeys

import scala.concurrent.ExecutionContext

/**
 * Concrete [[Authorization]] implementation that authorizes the user depending on the security roles.
 *
 * @param userService the [[UserService]] instance.
 * @param ec the [[ExecutionContext]] instance.
 * @tparam A The actual [[Authenticator]] type.
 */
case class WithRoleAccess[A <: Authenticator](role: SecurityRoleKeys.Type)(implicit userService: UserService, ec: ExecutionContext) extends Authorization[UserRow, A] {
  import UserService._

  def isAuthorized[B](user: UserRow, authenticator: A)(implicit request: Request[B]) = {
    user.hasRole(role)
  }
}
