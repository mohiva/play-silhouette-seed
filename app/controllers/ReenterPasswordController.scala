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
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param credentialsProvider    The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 * @param webJarsUtil            The webjar util.
 * @param assets                 The Play assets finder.
 */
class ReenterPasswordController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {
  /**
   * Views the `Reenter password` page.
   * @return The result to display.
   */
  def view = silhouette.SecuredAction.async { implicit request =>
    val data = Data(email = request.identity.email.getOrElse(""), password = "")
    Future.successful(Ok(views.html.reenterPassword(ReenterPasswordForm.form.fill(data), request.identity)))
  }

  /**
   * Handles the submitted form.
   * @return The result to display.
   */
  def submit = silhouette.SecuredAction.async { implicit request =>
    ReenterPasswordForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.reenterPassword(form, request.identity))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val result = Future.successful(request.session.get(SessionKeys.REDIRECT_TO_URI).map { targetUri =>
            Redirect(targetUri)
          }.getOrElse {
            Redirect(routes.ApplicationController.index())
          }.withSession(request.session + (SessionKeys.HAS_SUDO_ACCESS -> "true")))
          userService.retrieve(loginInfo).flatMap {
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
            case _ => result
          }
        }.recover {
          case _: ProviderException =>
            Redirect(routes.ReenterPasswordController.view()).flashing("error" -> Messages("current.password.invalid"))
        }
      }
    )
  }
}
