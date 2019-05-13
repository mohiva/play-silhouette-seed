package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import org.webjars.play.WebJarsUtil
import play.api.i18n.{ I18nSupport, Lang }
import play.api.mvc.{ AbstractController, ControllerComponents }
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param components  The Play controller components.
 * @param silhouette  The Silhouette stack.
 * @param webJarsUtil The webjar util.
 * @param assets      The Play assets finder.
 */
class ApplicationController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder
) extends AbstractController(components) with I18nSupport {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  /**
   * Handles language change.
   * @param lang The language to set
   * @return The result to display.
   */
  def selectLang(lang: String) = silhouette.UserAwareAction.async { implicit request =>
    request.headers.get(REFERER).map { referer =>
      Future.successful(Redirect(referer).withLang(Lang(lang)))
    }.getOrElse {
      Future.successful(Redirect(routes.ApplicationController.index).withLang(Lang(lang)))
    }
  }
}
