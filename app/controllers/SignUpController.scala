// scalastyle:off
package controllers

import java.util.UUID

import play.api.mvc._
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{PasswordInfo, PasswordHasher}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import models.{TokenUser, User}
import models.services.{TokenService, UserService}
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import utils.{MailService, Mailer}

import scala.concurrent.Future
import scala.reflect.ClassTag

/**
 * The sign up controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class SignUpController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  avatarService: AvatarService,
  passwordHasher: PasswordHasher,
  tokenService: TokenService[TokenUser],
  mailService: MailService)
  extends Silhouette[User, CookieAuthenticator] {

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  def signUpRequest = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.signUp(SignUpForm.formSignUp)))
      case unknown => Future.failed(new RuntimeException(s"request.identity returned an unexpected type $unknown"))
    }
  }

  /**
   * Registers a new user.
   *
   * @return The result to display.
   */
  def signUpRequestRegistration = Action.async { implicit request =>
    SignUpForm.formSignUp.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      signUpData => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, signUpData.email)

        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(Redirect(routes.SignUpController.signUpRequest()).flashing("error" -> Messages("user.exists")))

          case None =>
            val authInfo = passwordHasher.hash(signUpData.password)
            authInfoRepository.save(loginInfo, authInfo)
            val token = TokenUser(signUpData.email, true, signUpData.firstName, signUpData.lastName)
            tokenService.create(token)
            Mailer.welcome(signUpData, link = routes.SignUpController.signUpCompletion(token.id).absoluteURL())(mailService)
            Future.successful(Ok(views.html.auth.almostSignedUp(signUpData)))

          case unknown => Future.failed(new RuntimeException(s"userService.retrieve(loginInfo) returned an unexpected type $unknown"))
        }
      }
    )
  }

  def signUpCompletion(token: String) = Action.async { implicit request =>
    executeForToken(token, true, { tokenUser =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, tokenUser.email)

      authInfoRepository.find(loginInfo)(ClassTag(classOf[PasswordInfo])).flatMap {
        case Some(authInfo) =>
          val user = User(
            userID = UUID.randomUUID(),
            loginInfo = loginInfo,
            firstName = Some(tokenUser.firstName),
            lastName = Some(tokenUser.lastName),
            fullName = Some(tokenUser.firstName + " " + tokenUser.lastName),
            email = Some(tokenUser.email),
            avatarURL = None
          )
          for {
            avatar <- avatarService.retrieveURL(tokenUser.email)
            user <- userService.save(user.copy(avatarURL = avatar))
            //authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(loginInfo)
            value <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
          } yield {
            env.eventBus.publish(SignUpEvent(user, request, request2Messages))
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            result
          }
        case unknown => Future.failed(new RuntimeException(s"authInfoRepository.find(loginInfo) returned an unexpected type $unknown"))
      }
    })
  }

  // scalastyle:on

  private def executeForToken(token: String, isSignUp: Boolean, f: TokenUser => Future[Result]): Future[Result] = {
    tokenService.retrieve(token).flatMap[Result]{ optTokenUser =>
      optTokenUser match {
        case Some(t) if !t.isExpired && t.isSignUp == isSignUp => f(t)
        case _ => Future.successful(NotFound(views.html.auth.invalidToken()))
      }
    }
  }
}

