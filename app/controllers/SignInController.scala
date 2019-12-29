package controllers

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.{ SignInForm, TotpForm }
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.{ AnyContent, Request }
import utils.route.Calls

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign In` controller.
 */
class SignInController @Inject() (
  scc: SilhouetteControllerComponents,
  signIn: views.html.signIn,
  activateAccount: views.html.activateAccount,
  totp: views.html.totp
)(implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  /**
   * Views the `Sign In` page.
   *
   * @return The result to display.
   */
  def view = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(signIn(SignInForm.form, socialProviderRegistry)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(signIn(form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated =>
              Future.successful(Ok(activateAccount(data.email)))
            case Some(user) =>
              authInfoRepository.find[GoogleTotpInfo](user.loginInfo).flatMap {
                case Some(totpInfo) => Future.successful(Ok(totp(TotpForm.form.fill(TotpForm.Data(
                  user.userID, totpInfo.sharedKey, data.rememberMe)))))
                case _ => authenticateUser(user, data.rememberMe)
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            Redirect(Calls.signin).flashing("error" -> Messages("invalid.credentials"))
        }
      }
    )
  }
}
