package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.AuthenticationException
import com.mohiva.play.silhouette.api.services.AuthInfoService
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import models.User
import models.services.UserService
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ RequestHeader, Action }

import scala.concurrent.Future

/**
 * The credentials auth controller.
 *
 * @param env The Silhouette environment.
 */
class CredentialsAuthController @Inject() (
  implicit val env: Environment[User, SessionAuthenticator],
  val userService: UserService,
  val authInfoService: AuthInfoService) extends Silhouette[User, SessionAuthenticator] {

  /**
   * Implement this to return a result when the user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override protected def notAuthenticated(request: RequestHeader) = {
    Some(Future.successful(Redirect(routes.ApplicationController.signIn).flashing("error" -> Messages("invalid.credentials"))))
  }

  /**
   * Authenticates a user against the credentials provider.
   *
   * @return The result to display.
   */
  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      credentials => (env.providers.get(CredentialsProvider.ID) match {
        case Some(p: CredentialsProvider) => p.authenticate(credentials)
        case _ => Future.failed(new AuthenticationException(s"Cannot find credentials provider"))
      }).flatMap { loginInfo =>
        val result = Future.successful(Redirect(routes.ApplicationController.index))
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => env.authenticatorService.create(user.loginInfo).flatMap { authenticator =>
            env.eventBus.publish(LoginEvent(user, request, request2lang))
            env.authenticatorService.init(authenticator).flatMap(v => env.authenticatorService.embed(v, result))
          }
          case None => Future.failed(new AuthenticationException("Couldn't find user"))
        }
      }.recoverWith(exceptionHandler)
    )
  }
}
