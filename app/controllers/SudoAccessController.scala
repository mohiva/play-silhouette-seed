package controllers

import action.SudoAccessAuthorization
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.webjars.play.WebJarsUtil
import play.api.i18n._
import play.api.mvc._
import utils.auth.DefaultEnv
import com.mohiva.play.silhouette.api.actions._
import constants.SessionKeys
import scala.concurrent.{ ExecutionContext, Future }

class SudoAccessController @Inject() (
  silhouette: Silhouette[DefaultEnv]
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends InjectedController with I18nSupport {
  /**
   * A local error handler.
   */
  val errorHandler = new SecuredErrorHandler {
    override def onNotAuthenticated(implicit request: RequestHeader) = {
      Future.successful(Redirect(controllers.routes.SignInController.view()))
    }

    override def onNotAuthorized(implicit request: RequestHeader) = {
      Future.successful(Redirect(controllers.routes.SignInController.view()).
        flashing("error" -> Messages("reenter.password")).
        withSession(request.session + (SessionKeys.REDIRECT_TO_URI -> request.uri)))
    }
  }

  /**
   * Handles example restricted sudo access action.
   * @return The result to display.
   */
  def restrictedSudoAccess = silhouette.SecuredAction(errorHandler)(SudoAccessAuthorization[DefaultEnv#A]()).async { implicit request =>
    Future.successful(Ok(views.html.restrictedSudoAccess(request.identity)))
  }
}
