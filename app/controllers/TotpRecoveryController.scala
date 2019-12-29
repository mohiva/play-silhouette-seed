package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.TotpRecoveryForm
import javax.inject.Inject
import play.api.i18n.Messages

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `TOTP` controller.
 */
class TotpRecoveryController @Inject() (
  scc: SilhouetteControllerComponents,
  totpRecovery: views.html.totpRecovery
)(implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  /**
   * Views the TOTP recovery page.
   *
   * @param userID the user ID.
   * @param sharedKey the shared key associated to the user.
   * @param rememberMe the remember me flag.
   * @return The result to display.
   */
  def view(userID: UUID, sharedKey: String, rememberMe: Boolean) = UnsecuredAction.async { implicit request =>
    Future.successful(Ok(totpRecovery(TotpRecoveryForm.form.fill(TotpRecoveryForm.Data(userID, sharedKey, rememberMe)))))
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit = UnsecuredAction.async { implicit request =>
    TotpRecoveryForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(totpRecovery(form))),
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
