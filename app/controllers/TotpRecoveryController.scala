package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.TotpRecoveryForm
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
 * @param silhouette The Silhouette stack.
 * @param userService The user service implementation.
 * @param totpProvider The totp provider.
 * @param configuration The Play configuration.
 * @param clock The clock instance.
 * @param webJarsUtil The webjar util.
 * @param assets The Play assets finder.
 * @param ex The execution context.
 * @param authInfoRepository The auth info repository.
 */
class TotpRecoveryController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  totpProvider: TotpProvider,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext,
  authInfoRepository: AuthInfoRepository
) extends AbstractAuthController(silhouette, configuration, clock) with I18nSupport {

  /**
   * Views the `TOTP` recovery page.
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.totpRecovery(TotpRecoveryForm.form)))
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    TotpRecoveryForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.totpRecovery(form))),
      data => {
        userService.retrieve(data.userID).flatMap {
          case Some(user) => {
            authInfoRepository.find[TotpInfo](user.loginInfo).flatMap {
              case Some(totpInfo) =>
                totpProvider.authenticate(totpInfo, data.recoveryCode).flatMap {
                  case Some(_) => authenticateUser(user, data.rememberMe)
                  case _ => Future.successful(Redirect(routes.TotpRecoveryController.view()).flashing("error" -> Messages("invalid.recovery.code")))
                }.recover {
                  case _: ProviderException =>
                    Redirect(routes.TotpRecoveryController.view()).flashing("error" -> Messages("invalid.unexpected.totp"))
                }
              case _ => Future.successful(Redirect(routes.TotpRecoveryController.view()).flashing("error" -> Messages("invalid.unexpected.totp")))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
    )
  }
}
