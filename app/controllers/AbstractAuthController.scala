package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Clock
import models.User
import net.ceedubs.ficus.Ficus._
import org.webjars.play.WebJarsUtil
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * `AbstractAuthController` base with support methods to authenticate an user.
 *
 * @param silhouette The Silhouette stack.
 * @param configuration The Play configuration.
 * @param clock The clock instance.
 * @param webJarsUtil The webjar util.
 * @param assets The Play assets finder.
 * @param ex The execution context.
 */
abstract class AbstractAuthController(
  silhouette: Silhouette[DefaultEnv],
  configuration: Configuration,
  clock: Clock
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends InjectedController with I18nSupport {

  /**
   * Performs user authentication
   * @param user User data
   * @param rememberMe Remember me flag
   * @param request Initial request
   * @return The result to display.
   */
  protected def authenticateUser(user: User, rememberMe: Boolean)(implicit request: Request[_]): Future[AuthenticatorResult] = {
    val c = configuration.underlying
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.authenticatorService.create(user.loginInfo).map {
      case authenticator if rememberMe =>
        authenticator.copy(
          expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
          idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
          cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
        )
      case authenticator => authenticator
    }.flatMap { authenticator =>
      silhouette.env.eventBus.publish(LoginEvent(user, request))
      silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
        silhouette.env.authenticatorService.embed(v, result)
      }
    }
  }
}
