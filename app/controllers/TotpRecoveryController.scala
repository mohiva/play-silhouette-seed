package controllers

import java.util.UUID

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
  totpProvider: GoogleTotpProvider,
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
   * Views the TOTP recovery page.
   *
   * @param userID the user ID.
   * @param sharedKey the shared key associated to the user.
   * @param rememberMe the remember me flag.
   * @return The result to display.
   */
  def view(userID: UUID, sharedKey: String, rememberMe: Boolean) = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.totpRecovery(TotpRecoveryForm.form.fill(TotpRecoveryForm.Data(userID, sharedKey, rememberMe)))))
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    TotpRecoveryForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.totpRecovery(form))),
      data => {
        val totpRecoveryControllerRoute = routes.TotpRecoveryController.view(data.userID, data.sharedKey, data.rememberMe)
        userService.retrieve(data.userID).flatMap {
          case Some(user) => {
            authInfoRepository.find[GoogleTotpInfo](user.loginInfo).flatMap {
              case Some(totpInfo) =>
                totpProvider.authenticate(totpInfo, data.recoveryCode).flatMap {
                  case Some(updated) => {
                    authInfoRepository.update[GoogleTotpInfo](user.loginInfo, updated._2)
                    authenticateUser(user, data.rememberMe)
                  }
                  case _ => Future.successful(Redirect(totpRecoveryControllerRoute).flashing("error" -> Messages("invalid.recovery.code")))
                }.recover {
                  case _: ProviderException =>
                    Redirect(totpRecoveryControllerRoute).flashing("error" -> Messages("invalid.unexpected.totp"))
                }
              case _ => Future.successful(Redirect(totpRecoveryControllerRoute).flashing("error" -> Messages("invalid.unexpected.totp")))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
    )
  }
}
