package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Clock
import constants.SessionKeys
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
 * @param silhouette
 * @param configuration
 * @param clock
 * @param webJarsUtil
 * @param assets
 * @param ex
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

  protected def authenticateUser(user: User, rememberMe: Boolean)(implicit request: Request[_]): Future[AuthenticatorResult] = {
    val authenticatorExpiry = configuration.underlying.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry")
    val authenticatorIdleTimeout = configuration.underlying.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
    val cookieMaxAge = configuration.underlying.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")

    val result = request.session.get(SessionKeys.REDIRECT_TO_URI).map { targetUri =>
      Redirect(targetUri)
    }.getOrElse {
      Redirect(routes.ApplicationController.index())
    }.withSession(request.session + (SessionKeys.HAS_SUDO_ACCESS -> "true"))

    silhouette.env.authenticatorService.create(user.loginInfo).map {
      case authenticator if rememberMe =>
        authenticator.copy(
          expirationDateTime = clock.now + authenticatorExpiry,
          idleTimeout = authenticatorIdleTimeout,
          cookieMaxAge = cookieMaxAge
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
