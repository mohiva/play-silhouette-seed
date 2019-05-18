package controllers

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import constants.SessionKeys
import forms.{ SignInForm, TotpForm }
import models.User
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import org.webjars.play.WebJarsUtil
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.{ AbstractController, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign In` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param credentialsProvider    The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 * @param webJarsUtil            The webjar util.
 * @param assets                 The Play assets finder.
 */
class SignInController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /**
   * Views the `Sign In` page.
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signIn(SignInForm.form, socialProviderRegistry)))
  }

  /**
   * Handles the submitted form.
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated =>
              Future.successful(Ok(views.html.activateAccount(data.email)))
            case Some(user) =>
              if (user.sharedKey.isEmpty) {
                authenticateUser(user, data.rememberMe)
              } else {
                Future.successful(Ok(views.html.totp(TotpForm.form.fill(TotpForm.Data(user.userID, user.sharedKey.get, data.rememberMe)))))
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.credentials"))
        }
      }
    )
  }

  def authenticateUser(user: User, rememberMe: Boolean)(implicit request: Request[_]): Future[AuthenticatorResult] = {
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
