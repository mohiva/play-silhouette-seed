package utils.auth

import com.mohiva.play.silhouette.api.Authorization
import models.User
import play.api.mvc.Request

import scala.concurrent.Future

/**
 * Grants only access if a user has authenticated with the given provider.
 *
 * @param provider The provider ID the user must authenticated with.
 */
case class WithProvider(provider: String) extends Authorization[User, DefaultEnv#A] {

  /**
   * Indicates if a user is authorized to access an action.
   *
   * @param user The usr object.
   * @param authenticator The authenticator instance.
   * @param request The current request.
   * @tparam B The type of the request body.
   * @return True if the user is authorized, false otherwise.
   */
  override def isAuthorized[B](user: User, authenticator: DefaultEnv#A)(
    implicit
    request: Request[B]): Future[Boolean] = {

    Future.successful(user.loginInfo.providerID == provider)
  }
}
