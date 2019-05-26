package controllers

import action.SudoAccessAuthorization
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.webjars.play.WebJarsUtil
import play.api.i18n._
import play.api.mvc._
import utils.auth.DefaultEnv
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import constants.SessionKeys
import models.services.UserService

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `SudoAccessController` implementation
 *
 * @param silhouette
 * @param webJarsUtil
 * @param assets
 * @param ec
 */
class SudoAccessController @Inject() (
  silhouette: Silhouette[DefaultEnv]
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  userService: UserService,
  ec: ExecutionContext
) extends InjectedController with I18nSupport {
  import UserService._

  /**
   * A local error handler.
   */
  val errorHandler = new SecuredErrorHandler {
    override def onNotAuthenticated(implicit request: RequestHeader) = {
      Future.successful(Redirect(controllers.routes.ReenterPasswordController.view()))
    }

    override def onNotAuthorized(implicit request: RequestHeader) = {
      Future.successful(Redirect(controllers.routes.ReenterPasswordController.view()).
        withSession(request.session + (SessionKeys.REDIRECT_TO_URI -> request.uri)))
    }
  }

  /**
   * Handles example restricted sudo access action.
   * @return The result to display.
   */
  def restrictedSudoAccess = silhouette.SecuredAction(errorHandler)(SudoAccessAuthorization[DefaultEnv#A]()).async { implicit request =>
    request.identity.loginInfo.flatMap {
      case Some(loginInfo) => Future.successful(Ok(views.html.restrictedSudoAccess(request.identity, loginInfo)))
      case _ => Future.failed(new IdentityNotFoundException("User doesn't have a LoginInfo attached"))
    }
  }
}
