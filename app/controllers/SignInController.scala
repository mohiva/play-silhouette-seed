package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.{ SignInForm, TotpForm }
import javax.inject.Inject
import models.services.UserService
import org.webjars.play.WebJarsUtil
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.ControllerComponents
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign In` controller.
 *
 * @param components
 * @param silhouette
 * @param credentialsProvider
 * @param socialProviderRegistry
 * @param authInfoRepository
 * @param passwordHasherRegistry
 * @param configuration
 * @param clock
 * @param webJarsUtil
 * @param assets
 * @param userService
 * @param ec
 */
class SignInController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
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
  userService: UserService,
  ec: ExecutionContext
) extends AbstractAuthController(silhouette, configuration, clock) with I18nSupport {
  import UserService._

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
            case Some(user) => {
              user.loginInfo.flatMap {
                case Some(loginInfo) =>
                  authInfoRepository.find[TotpInfo](loginInfo).flatMap {
                    case Some(totpInfo) => Future.successful(Ok(views.html.totp(TotpForm.form.fill(TotpForm.Data(
                      user.id, totpInfo.sharedKey, data.rememberMe)))))
                    case _ => authenticateUser(user, data.rememberMe)
                  }
                case _ => Future.failed(new IdentityNotFoundException("User doesn't have a LoginInfo attached"))
              }
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
}
