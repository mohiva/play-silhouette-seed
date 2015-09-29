package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{PasswordInfo, PasswordHasher}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
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
    avatarService: AvatarService,
    passwordHasher: PasswordHasher,
    tokenService: TokenService[TokenUser],
    mailService: MailService)
  extends Silhouette[User, CookieAuthenticator] {

  val providerId = CredentialsProvider.ID
  val Email = "email"


  /*
   * PASSWORD RESET
   */

  val pwResetForm = Form[String] (
    Email -> email.verifying( nonEmpty )
  )

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

  val passwordsForm = Form( tuple(
    "password1" -> nonEmptyText(minLength = 6),
    "password2" -> nonEmptyText,
    "token" -> nonEmptyText
  ) verifying(Messages("passwords.not.equal"), passwords => passwords._2 == passwords._1 ))

  case class ChangeInfo(currentPassword: String, newPassword: String)

  private def notFoundDefault (implicit request: RequestHeader) =
    Future.successful(NotFound(views.html.auth.invalidToken()))

  /**
   * Confirms the user's link based on the token and shows him a form to reset the password
   */
  def resetPassword (tokenId: String) = Action.async { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if (!token.isSignUp && !token.isExpired) => {
        Future.successful(Ok(views.html.auth.specifyResetPassword(tokenId, passwordsForm)))
      }
      case Some(token) => {
        tokenService.consume(tokenId)
        notFoundDefault
      }
      case None => notFoundDefault
    }
  }

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
}
