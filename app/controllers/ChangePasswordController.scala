package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Credentials, PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import constants.SessionKeys
import forms.ChangePasswordForm
import models.services.UserService
import org.webjars.play.WebJarsUtil
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents }
import utils.auth.{ DefaultEnv, WithProvider }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Change Password` controller.
 *
 * @param components The Play controller components.
 * @param silhouette The Silhouette stack.
 * @param credentialsProvider The credentials provider.
 * @param authInfoRepository The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 * @param webJarsUtil The webjar util.
 * @param assets The Play assets finder.
 * @param userService The user service implementation.
 * @param ec The execution context.
 */
class ChangePasswordController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  credentialsProvider: CredentialsProvider,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  userService: UserService,
  ec: ExecutionContext
) extends AbstractController(components) with I18nSupport {
  import UserService._

  /**
   * Views the `Change Password` page.
   * @return The result to display.
   */
  def view = silhouette.SecuredAction(WithProvider[DefaultEnv#A](CredentialsProvider.ID)).async {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
      request.identity.loginInfo.flatMap {
        case Some(loginInfo) => Future.successful(Ok(views.html.changePassword(ChangePasswordForm.form, request.identity, loginInfo)))
        case _ => Future.failed(new IllegalStateException(Messages("internal.error.user.without.logininfo")))
      }
  }

  /**
   * Changes the password.
   * @return The result to display.
   */
  def submit = silhouette.SecuredAction(WithProvider[DefaultEnv#A](CredentialsProvider.ID)).async {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
      request.identity.loginInfo.flatMap {
        case Some(loginInfo) =>
          ChangePasswordForm.form.bindFromRequest.fold(
            form => Future.successful(BadRequest(views.html.changePassword(form, request.identity, loginInfo))),
            data => {
              val credentials = Credentials(request.identity.email.getOrElse(""), data.currentPassword)
              credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
                val passwordInfo = passwordHasherRegistry.current.hash(data.newPassword)
                authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
                  Redirect(routes.ChangePasswordController.view()).
                    flashing("success" -> Messages("password.changed")).
                    withSession(request.session - SessionKeys.HAS_SUDO_ACCESS - SessionKeys.REDIRECT_TO_URI)
                }
              }.recover {
                case _: ProviderException =>
                  Redirect(routes.ChangePasswordController.view()).
                    flashing("error" -> Messages("current.password.invalid")).
                    withSession(request.session - SessionKeys.HAS_SUDO_ACCESS - SessionKeys.REDIRECT_TO_URI)
              }
            }
          )
        case _ => Future.failed(new IllegalStateException(Messages("internal.error.user.without.logininfo")))
      }
  }
}
