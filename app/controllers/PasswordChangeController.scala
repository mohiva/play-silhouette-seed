// scalastyle:off
package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordInfo, PasswordHasher}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import controllers.PasswordChangeController.ChangeInfo
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.{ MessagesApi, Messages }
import play.api.mvc._
import play.api.{Logger}
import play.api.libs.concurrent.Execution.Implicits._
import utils.{MailService, Mailer}

import scala.language.postfixOps
import scala.concurrent.Future
import scala.reflect.ClassTag

import models.{TokenUser, User}
import models.services.{TokenService, UserService}


/**
 * A controller to provide password change functionality
 */
class PasswordChangeController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[User, CookieAuthenticator],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    avatarService: AvatarService,
    passwordHasher: PasswordHasher,
    tokenService: TokenService[TokenUser],
    mailService: MailService)
  extends Silhouette[User, CookieAuthenticator] {

  val providerId = CredentialsProvider.ID
  val Email = "email"
  val passwordValidation = nonEmptyText(minLength = 6)

  /*
   * PASSWORD RESET  - When user has forgotten their password and can't login
   */

  val pwResetForm = Form[String] (
    Email -> email.verifying( nonEmpty )
  )

  val passwordsForm = Form( tuple(
    "password1" -> passwordValidation,
    "password2" -> nonEmptyText,
    "token" -> nonEmptyText
  ) verifying(Messages("passwords.not.equal"), passwords => passwords._2 == passwords._1 ))

  private def notFoundDefault (implicit request: RequestHeader) =
    Future.successful(NotFound(views.html.auth.invalidToken()))

  def startResetPassword = Action.async { implicit request =>
    Future.successful(Ok(views.html.auth.startResetPassword(pwResetForm)))
  }

  def handleStartResetPassword = Action.async { implicit request =>
    pwResetForm.bindFromRequest.fold (
      errors => Future.successful(BadRequest(views.html.auth.startResetPassword(errors))),
      email => {
        authInfoRepository.find(LoginInfo(CredentialsProvider.ID,email))(ClassTag(classOf[PasswordInfo])).map {
          case Some(user) => {
            val token = TokenUser(email)
            tokenService.create(token)

            Mailer.forgotPassword(email, link = routes.PasswordChangeController.specifyResetPassword(token.id).absoluteURL())(mailService)
          }
          case None => {
            Mailer.forgotPasswordUnknowAddress(email)(mailService)
          }
        }
        Future.successful(Ok(views.html.auth.sentResetPassword(email)))
      }
    )
  }

  /**
   * Confirms the user's link based on the token and shows them a form to reset the password
   */
  def specifyResetPassword (tokenId: String) = Action.async { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if (!token.isSignUp && !token.isExpired) => {
        Future.successful(Ok(views.html.auth.specifyResetPassword(tokenId, passwordsForm)))
      }
      case Some(token) => {
        tokenService.consume(tokenId)
        notFoundDefault
      }
      case None => {
        notFoundDefault
      }
    }
  }

  /**
   * Saves the new password and authenticates the user
   */
  def handleResetPassword = Action.async { implicit request =>
    passwordsForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.auth.specifyResetPassword(formWithErrors.data("token"), formWithErrors))),
      passwords => {
        val tokenId = passwords._3
        tokenService.retrieve(tokenId).flatMap {
          case Some(token) if (!token.isSignUp && !token.isExpired) => {
            val loginInfo = LoginInfo(CredentialsProvider.ID, token.email)
            userService.retrieve(loginInfo).flatMap {
              case Some(user) => {
                val authInfo = passwordHasher.hash(passwords._1)
                authInfoRepository.save(loginInfo, authInfo)
                env.authenticatorService.create(user.loginInfo).flatMap { authenticator =>
                  env.eventBus.publish(LoginEvent(user, request, request2Messages))
                  tokenService.consume(tokenId)
                  env.authenticatorService.init(authenticator)
                  Future.successful(Ok(views.html.auth.confirmResetPassword(user)))
                }
              }
              case None => Future.failed(new RuntimeException("Couldn't find user"))
            }
          }
          case Some(token) => {
            tokenService.consume(tokenId)
            notFoundDefault
          }
          case None => {
            notFoundDefault
          }
        }
      }
    )
  }

  /*
   * CHANGE PASSWORD - Can only be done whilst user is logged in
   */

  val changePasswordForm = Form[ChangeInfo](
    mapping(
      "currentPassword" -> nonEmptyText,
      "newPassword" -> tuple(
        "password1" -> passwordValidation,
        "password2" -> nonEmptyText
      ).verifying( Messages("passwords.not.equal"), newPassword => newPassword._2 == newPassword._1 )
    )
      ((currentPassword, newPassword) => ChangeInfo(currentPassword, newPassword._1)) //apply
      (data => Some((data.currentPassword, (data.newPassword, data.newPassword))))    //unapply
  )

  def startChangePassword = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.auth.changePassword(request.identity,changePasswordForm)))
  }

  /**
   * Saves the new password and authenticates the user
   */
  def handleChangePassword = SecuredAction.async { implicit request =>
    changePasswordForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.auth.changePassword(request.identity,formWithErrors))),
      changeInfo => {
        val user = request.identity

        credentialsProvider.authenticate( Credentials(user.email.getOrElse(""), changeInfo.currentPassword) ).flatMap { loginInfo =>
          authInfoRepository.save(loginInfo, passwordHasher.hash(changeInfo.newPassword))
          env.authenticatorService.create(user.loginInfo).flatMap { authenticator =>
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            env.authenticatorService.init(authenticator)
            Future.successful(Ok(views.html.auth.confirmResetPassword(user)))
          }
        }.recover {
          case e: ProviderException =>
            BadRequest(views.html.auth.changePassword(request.identity,changePasswordForm.withError("currentPassword","Does not match current password!")))
        }
      }
    )
  }
}

object PasswordChangeController {
  case class ChangeInfo(currentPassword: String, newPassword: String)
}
// scalastyle:on