package controllers

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import constants.SessionKeys
import forms.ResetPasswordForm
import models.services.{ AuthTokenService, UserService }
import org.webjars.play.WebJarsUtil
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Reset Password` controller.
 *
 * @param components The Play controller components.
 * @param silhouette The Silhouette stack.
 * @param authInfoRepository The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 * @param authTokenService The auth token service implementation.
 * @param webJarsUtil The webjar util.
 * @param assets The Play assets finder.
 * @param userService The user service implementation.
 * @param ec The execution context.
 */
class ResetPasswordController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: AuthTokenService
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  userService: UserService,
  ec: ExecutionContext
) extends AbstractController(components) with I18nSupport {
  import UserService._

  /**
   * Views the `Reset Password` page.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def view(token: UUID) = silhouette.UnsecuredAction.async { implicit request =>
    authTokenService.validate(token).map {
      case Some(_) => Ok(views.html.resetPassword(ResetPasswordForm.form, token))
      case None => Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.reset.link"))
    }
  }

  /**
   * Resets the password.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def submit(token: UUID) = silhouette.UnsecuredAction.async { implicit request =>
    val invalidResetLinkRedirect = Redirect(routes.SignInController.view()).
      flashing("error" -> Messages("invalid.reset.link")).
      withSession(request.session - SessionKeys.HAS_SUDO_ACCESS - SessionKeys.REDIRECT_TO_URI)
    authTokenService.validate(token).flatMap {
      case Some(authToken) =>
        ResetPasswordForm.form.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.resetPassword(form, token))),
          data => userService.retrieve(authToken.userID).flatMap {
            case Some(user) => {
              user.loginInfo.flatMap {
                case Some(loginInfo) => {
                  if (loginInfo.providerID == CredentialsProvider.ID) {
                    val passwordInfo = passwordHasherRegistry.current.hash(data.password)
                    authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
                      Redirect(routes.SignInController.view()).flashing("success" -> Messages("password.reset")).
                        withSession(request.session - SessionKeys.HAS_SUDO_ACCESS - SessionKeys.REDIRECT_TO_URI)
                    }
                  } else {
                    Future.successful(invalidResetLinkRedirect)
                  }
                }
                case _ => Future.failed(new IdentityNotFoundException("User doesn't have a LoginInfo attached"))
              }
            }
            case _ => Future.successful(invalidResetLinkRedirect)
          }
        )
      case None => Future.successful(invalidResetLinkRedirect)
    }
  }
}
