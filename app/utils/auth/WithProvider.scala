package utils.auth

import com.mohiva.play.silhouette.api.{ Authenticator, Authorization }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import models.generated.Tables.UserRow
import models.services.UserService
import play.api.mvc.Request

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Grants only access if a user has authenticated with the given provider.
 *
 * @param provider The provider ID the user must authenticated with.
 * @tparam A The type of the authenticator.
 */
case class WithProvider[A <: Authenticator](provider: String)(implicit userService: UserService, ec: ExecutionContext) extends Authorization[UserRow, A] {
  /**
   * Indicates if a user is authorized to access an action.
   *
   * @param user The usr object.
   * @param authenticator The authenticator instance.
   * @param request The current request.
   * @tparam B The type of the request body.
   * @return True if the user is authorized, false otherwise.
   */
  override def isAuthorized[B](user: UserRow, authenticator: A)(implicit request: Request[B]): Future[Boolean] = {
    import UserService._
    user.loginInfo.flatMap {
      case Some(loginInfo) => Future.successful(loginInfo.providerID == provider)
      case _ => Future.failed(new IdentityNotFoundException("User doesn't have a LoginInfo attached"))
    }
  }
}
