package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.{ TotpForm, TotpSetupForm }
import javax.inject.Inject
import play.api.i18n.Messages
import utils.route.Calls

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `TOTP` controller.
 */
class TotpController @Inject() (
  scc: SilhouetteControllerComponents,
  totp: views.html.totp,
  home: views.html.home
)(implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  /**
   * Views the `TOTP` page.
   * @return The result to display.
   */
  def view(userId: java.util.UUID, sharedKey: String, rememberMe: Boolean) = UnsecuredAction.async { implicit request =>
    Future.successful(Ok(totp(TotpForm.form.fill(TotpForm.Data(userId, sharedKey, rememberMe)))))
  }

  /**
   * Enable TOTP.
   * @return The result to display.
   */
  def enableTotp = SecuredAction.async { implicit request =>
    val user = request.identity
    val credentials = totpProvider.createCredentials(user.email.get)
    val totpInfo = credentials.totpInfo
    val formData = TotpSetupForm.form.fill(TotpSetupForm.Data(totpInfo.sharedKey, totpInfo.scratchCodes, credentials.scratchCodesPlain))
    authInfoRepository.find[GoogleTotpInfo](request.identity.loginInfo).map { totpInfoOpt =>
      Ok(home(user, totpInfoOpt, Some((formData, credentials))))
    }
  }

  /**
   * Disable TOTP.
   * @return The result to display.
   */
  def disableTotp = SecuredAction.async { implicit request =>
    val user = request.identity
    authInfoRepository.remove[GoogleTotpInfo](user.loginInfo)
    Future(Redirect(Calls.home).flashing("info" -> Messages("totp.disabling.info")))
  }

  /**
   * Handles the submitted form with TOTP initial data.
   * @return The result to display.
   */
  def enableTotpSubmit = SecuredAction.async { implicit request =>
    val user = request.identity
    TotpSetupForm.form.bindFromRequest.fold(
      form => authInfoRepository.find[GoogleTotpInfo](request.identity.loginInfo).map { totpInfoOpt =>
        BadRequest(home(user, totpInfoOpt))
      },
      data => {
        totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
          case Some(loginInfo: LoginInfo) => {
            authInfoRepository.add[GoogleTotpInfo](user.loginInfo, GoogleTotpInfo(data.sharedKey, data.scratchCodes))
            Future(Redirect(Calls.home).flashing("success" -> Messages("totp.enabling.info")))
          }
          case _ => Future.successful(Redirect(Calls.home).flashing("error" -> Messages("invalid.verification.code")))
        }.recover {
          case _: ProviderException =>
            Redirect(routes.TotpController.view(user.userID, data.sharedKey, request.authenticator.cookieMaxAge.isDefined)).flashing("error" -> Messages("invalid.unexpected.totp"))
        }
      }
    )
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit = UnsecuredAction.async { implicit request =>
    TotpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(totp(form))),
      data => {
        val totpControllerRoute = routes.TotpController.view(data.userID, data.sharedKey, data.rememberMe)
        userService.retrieve(data.userID).flatMap {
          case Some(user) =>
            totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
              case Some(_) => authenticateUser(user, data.rememberMe)
              case _ => Future.successful(Redirect(totpControllerRoute).flashing("error" -> Messages("invalid.verification.code")))
            }.recover {
              case _: ProviderException =>
                Redirect(totpControllerRoute).flashing("error" -> Messages("invalid.unexpected.totp"))
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
    )
  }
}
