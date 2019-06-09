package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import constants.SessionKeys
import forms.ReenterPasswordForm
import forms.ReenterPasswordForm.Data
import javax.inject.Inject
import models.services.UserService
import org.webjars.play.WebJarsUtil
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.{ AbstractController, ControllerComponents }
import utils.auth.DefaultEnv
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Reenter password` controller.
 *
 * @param components the Play controller components.
 * @param silhouette the Silhouette stack.
 * @param credentialsProvider the credentials provider.
 * @param socialProviderRegistry the social provider registry.
 * @param configuration the Play configuration.
 * @param clock the clock instance.
 * @param webJarsUtil the webjar util.
 * @param assets the Play assets finder.
 * @param userService the user service implementation.
 * @param ec an ExecutionContext instance.
 */
class ReenterPasswordController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  userService: UserService,
  ec: ExecutionContext
) extends AbstractController(components) with I18nSupport {
  import UserService._

  /**
   * Views the `Reenter password` page.
   * @return The result to display.
   */
  def view = silhouette.SecuredAction.async { implicit request =>
    val data = Data(email = request.identity.email, password = "")
    request.identity.loginInfo.flatMap {
      case Some(loginInfo) => Future.successful(Ok(views.html.reenterPassword(ReenterPasswordForm.form.fill(data), request.identity, loginInfo)))
      case _ => Future.failed(new IllegalStateException(Messages("internal.error.user.without.logininfo")))
    }
  }

  /**
   * Handles the submitted form.
   * @return The result to display.
   */
  def submit = silhouette.SecuredAction.async { implicit request =>
    request.identity.loginInfo.flatMap {
      case Some(loginInfo) =>
        ReenterPasswordForm.form.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.reenterPassword(form, request.identity, loginInfo))),
          data => {
            val credentials = Credentials(data.email, data.password)
            credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
              val result = Future.successful(request.session.get(SessionKeys.REDIRECT_TO_URI).map { targetUri =>
                Redirect(targetUri)
              }.getOrElse {
                Redirect(routes.ApplicationController.index())
              }.withSession(request.session + (SessionKeys.HAS_SUDO_ACCESS -> "true")))
              userService.retrieve(loginInfo).flatMap {
                case None => Future.failed(new IdentityNotFoundException(Messages("internal.error.no.user.found")))
                case _ => result
              }
            }.recover {
              case _: ProviderException =>
                Redirect(routes.ReenterPasswordController.view()).flashing("error" -> Messages("current.password.invalid"))
            }
          }
        )
      case _ => Future.failed(new IllegalStateException(Messages("internal.error.user.without.logininfo")))
    }
  }
}
