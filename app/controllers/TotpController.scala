package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
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
 * @param silhouette The Silhouette stack.
 * @param totpProvider The totp provider.
 * @param configuration The Play configuration.
 * @param clock The clock instance.
 * @param webJarsUtil The webjar util.
 * @param assets The Play assets finder.
 * @param userService The user service implementation.
 * @param ec The execution context.
 * @param authInfoRepository The auth info repository.
 */
class TotpController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  totpProvider: TotpProvider,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  userService: UserService,
  ec: ExecutionContext,
  authInfoRepository: AuthInfoRepository
) extends AbstractAuthController(silhouette, configuration, clock) with I18nSupport {
  import UserService._

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
    val totpInfo = credentials.totpInfo
    val formData = TotpSetupForm.form.fill(TotpSetupForm.Data(totpInfo.sharedKey, totpInfo.scratchCodes, credentials.scratchCodesPlain))
    request.identity.loginInfo.flatMap {
      case Some(loginInfo) => {
        authInfoRepository.find[TotpInfo](loginInfo).map { totpInfoOpt =>
          Ok(views.html.home(user, loginInfo, totpInfoOpt, Some((formData, credentials))))
        }
      }
      case _ => Future.failed(new IdentityNotFoundException("User doesn't have a LoginInfo attached"))
    }
  }

  /**
   * Disable TOTP.
   * @return The result to display.
   */
  def disableTotp = silhouette.SecuredAction.async { implicit request =>
    val user = request.identity
    user.loginInfo.flatMap {
      case Some(loginInfo) => {
        authInfoRepository.remove[TotpInfo](loginInfo)
        Future(Redirect(routes.ApplicationController.index()).flashing("info" -> Messages("totp.disabling.info")))
      }
      case _ => Future.failed(new IdentityNotFoundException("User doesn't have a LoginInfo attached"))
    }
  }

  /**
   * Handles the submitted form with TOTP initial data.
   * @return The result to display.
   */
  def enableTotpSubmit = silhouette.SecuredAction.async { implicit request =>
    val user = request.identity
    user.loginInfo.flatMap {
      case Some(loginInfo) => {
        TotpSetupForm.form.bindFromRequest.fold(
          form => authInfoRepository.find[TotpInfo](loginInfo).map { totpInfoOpt =>
            BadRequest(views.html.home(user, loginInfo, totpInfoOpt))
          },
          data => {
            totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
              case Some(loginInfo: LoginInfo) => {
                authInfoRepository.add[TotpInfo](loginInfo, TotpInfo(data.sharedKey, data.scratchCodes))
                Future(Redirect(routes.ApplicationController.index()).flashing("success" -> Messages("totp.enabling.info")))
              }
              case _ => Future.successful(Redirect(routes.ApplicationController.index()).flashing("error" -> Messages("invalid.verification.code")))
            }.recover {
              case _: ProviderException =>
                Redirect(routes.TotpController.view()).flashing("error" -> Messages("invalid.unexpected.totp"))
            }
          }
        )
      }
      case _ => Future.failed(new IdentityNotFoundException("User doesn't have a LoginInfo attached"))
    }
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    TotpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.totp(form))),
      data => {
        userService.retrieve(data.userId).flatMap {
          case Some(user) =>
            totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
              case Some(_) => authenticateUser(user, data.rememberMe)
              case _ => Future.successful(Redirect(routes.TotpController.view()).flashing("error" -> Messages("invalid.verification.code")))
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
