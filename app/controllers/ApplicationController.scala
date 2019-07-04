package controllers

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import javax.inject.Inject
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.GoogleTotpInfo
import models.services.UserService
import org.webjars.play.WebJarsUtil
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
 * The basic application controller.
 * @param components The Play controller components.
 * @param silhouette The Silhouette stack.
 * @param authInfoRepository The auth information repository.
 * @param webJarsUtil The webjar util.
 * @param assets The Play assets finder.
 * @param userService The user service implementation.
 * @param ec The Execution context.
 */
class ApplicationController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  authInfoRepository: AuthInfoRepository
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  userService: UserService,
  ec: ExecutionContext,
) extends AbstractController(components) with I18nSupport {
  import UserService._

  /**
   * Handles the index action.
   * @return The result to display.
   */
  def index = silhouette.SecuredAction.async { implicit request =>
    request.identity.loginInfo.flatMap {
      case Some(loginInfo) => {
        authInfoRepository.find[GoogleTotpInfo](loginInfo).map { totpInfoOpt =>
          Ok(views.html.index(request.identity, loginInfo, totpInfoOpt))
        }
      }
      case _ => Future.failed(new IllegalStateException(Messages("internal.error.user.without.logininfo")))
    }
  }

  /**
   * Handles the Sign Out action.
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index()).withNewSession
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
