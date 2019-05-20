package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.{ TotpForm, TotpSetupForm }
import javax.inject.Inject
import models.services.UserService
import org.webjars.play.WebJarsUtil
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `TOTP` controller.
 *
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param totpProvider           The totp provider.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 * @param webJarsUtil            The webjar util.
 * @param assets                 The Play assets finder.
 */
class TotpController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  totpProvider: TotpProvider,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends AbstractAuthController(silhouette, configuration, clock) with I18nSupport {

  /**
   * Views the `TOTP` page.
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.totp(TotpForm.form)))
  }

  /**
   * Enable TOTP.
   * @return The result to display.
   */
  def enableTotp = silhouette.SecuredAction.async { implicit request =>
    val user = request.identity
    val credentials = totpProvider.createCredentials(user.email.get)
    val formData = TotpSetupForm.form.fill(TotpSetupForm.Data(credentials.sharedKey))
    Future.successful(Ok(views.html.home(user, Some((formData, credentials)))))
  }

  /**
   * Disable TOTP.
   * @return The result to display.
   */
  def disableTotp = silhouette.SecuredAction.async { implicit request =>
    val user = request.identity
    userService.save(user.copy(sharedKey = None))
    Future(Redirect(routes.ApplicationController.index()).flashing("info" -> Messages("totp.disabling.info")))
  }

  /**
   * Handles the submitted form with TOTP initial data.
   * @return The result to display.
   */
  def enableTotpSubmit = silhouette.SecuredAction.async { implicit request =>
    val user = request.identity
    TotpSetupForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.home(user))),
      data => {
        totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap { loginInfoOpt =>
          loginInfoOpt match {
            case Some(loginInfo) => {
              userService.save(user.copy(sharedKey = Some(loginInfo.providerKey)))
              Future(Redirect(routes.ApplicationController.index()).flashing("info" -> Messages("totp.enabling.info")))
            }
            case _ => Future.successful(Redirect(routes.ApplicationController.index()).flashing("error" -> Messages("invalid.verificationCode")))
          }
        }.recover {
          case _: ProviderException =>
            Redirect(routes.TotpController.view()).flashing("error" -> Messages("invalid.unexpected.totp"))
        }
      }
    )
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    TotpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.totp(form))),
      data => {
        userService.retrieve(data.userID).flatMap {
          case Some(user) =>
            totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap { loginInfoOpt =>
              loginInfoOpt match {
                case Some(_) => authenticateUser(user, data.rememberMe)
                case _ => Future.successful(Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.verificationCode")))
              }
            }.recover {
              case _: ProviderException =>
                Redirect(routes.TotpController.view()).flashing("error" -> Messages("invalid.unexpected.totp"))
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
    )
  }
}
