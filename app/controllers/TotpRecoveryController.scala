package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.TotpRecoveryForm
import javax.inject.Inject
import models.daos.ScratchCodeDao
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
class TotpRecoveryController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  totpProvider: TotpProvider,
  configuration: Configuration,
  clock: Clock,
  scratchCodeDao: ScratchCodeDao
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
   * Views the TOTP recovery page.
   *
   * @param userId the user ID.
   * @param sharedKey the shared key associated to the user.
   * @param rememberMe the remember me flag.
   * @return The result to display.
   */
  def view(userId: Long, sharedKey: String, rememberMe: Boolean) = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.totpRecovery(TotpRecoveryForm.form.fill(TotpRecoveryForm.Data(userId, sharedKey, rememberMe)))))
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    TotpRecoveryForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.totpRecovery(form))),
      data => {
        val totpRecoveryControllerRoute = routes.TotpRecoveryController.view(data.userId, data.sharedKey, data.rememberMe)
        userService.retrieve(data.userId).flatMap {
          case Some(user) => {
            user.loginInfo.flatMap {
              case Some(loginInfo) => {
                authInfoRepository.find[TotpInfo](loginInfo).flatMap {
                  case Some(totpInfo) =>
                    totpProvider.authenticate(totpInfo, data.recoveryCode).flatMap {
                      case Some((deleted, updated)) => {
                        authInfoRepository.update[TotpInfo](loginInfo, updated)
                        scratchCodeDao.delete(user.id, deleted)
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
              case _ => Future.failed(new IllegalStateException(Messages("internal.error.user.without.logininfo")))
            }
          }
          case None => Future.failed(new IdentityNotFoundException(Messages("internal.error.no.user.found")))
        }
      }
    )
  }
}
