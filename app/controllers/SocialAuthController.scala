package controllers

import javax.inject.Inject
import scala.concurrent.Future
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.services.AuthInfoService
import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import models.services.UserService
import models.User

/**
 * The social auth controller.
 *
 * @param env The Silhouette environment.
 */
class SocialAuthController @Inject() (
  val env: Environment[User, CachedCookieAuthenticator],
  val userService: UserService,
  val authInfoService: AuthInfoService)
  extends Silhouette[User, CachedCookieAuthenticator] {

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = Action.async { implicit request =>
    (env.providers.get(provider) match {
      case Some(p: SocialProvider[_] with CommonSocialProfileBuilder[_]) => p.authenticate()
      case _ => Future.failed(new AuthenticationException(s"Cannot authenticate with unexpected social provider $provider"))
    }).flatMap {
      case Left(result) => Future.successful(result)
      case Right(profile: CommonSocialProfile[_]) =>
        for {
          user <- userService.save(profile)
          authInfo <- authInfoService.save(profile.loginInfo, profile.authInfo)
          maybeAuthenticator <- env.authenticatorService.create(user)
        } yield {
          maybeAuthenticator match {
            case Some(authenticator) =>
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.send(authenticator, Redirect(routes.ApplicationController.index))
            case None => throw new AuthenticationException("Couldn't create an authenticator")
          }
        }
    }.recoverWith(exceptionHandler)
  }
}
