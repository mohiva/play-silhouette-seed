package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorService, AvatarService }
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, GoogleTotpProvider, SocialProviderRegistry }
import javax.inject.Inject
import models.services.{ AuthTokenService, UserService }
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{ Langs, MessagesApi }
import play.api.libs.mailer.MailerClient
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.FiniteDuration

import scala.language.higherKinds

abstract class SilhouetteController[E <: Env](
  override protected val controllerComponents: SilhouetteControllerComponents[E])
  extends MessagesAbstractController(controllerComponents) with SilhouetteComponents[E] with Logging {

  type SecuredEnvRequest[A] = SecuredRequest[EnvType, A]
  type AppSecuredEnvRequest[A] = AppSecuredRequest[EnvType, A]
  type UserAwareEnvRequest[A] = UserAwareRequest[EnvType, A]
  type AppUserAwareEnvRequest[A] = AppUserAwareRequest[EnvType, A]

  /*
  * Abstract class to stop defining executionContext in every subclass
  *
  * @param cc controller components
    */
  protected abstract class AbstractActionTransformer[-R[_], +P[_]] extends ActionTransformer[R, P] {
    override protected def executionContext: ExecutionContext =
      controllerComponents.executionContext
  }

  /**
   * Transforms from a Request into AppRequest.
   */
  class AppActionTransformer extends AbstractActionTransformer[Request, AppRequest] {
    override protected def transform[A](request: Request[A]): Future[AppRequest[A]] = {
      Future.successful(new AppRequest[A](
        messagesApi = controllerComponents.messagesApi,
        request = request
      ))
    }
  }

  /**
   * Transforms from a SecuredRequest into AppSecuredRequest.
   */
  class AppSecuredActionTransformer extends AbstractActionTransformer[SecuredEnvRequest, AppSecuredEnvRequest] {
    override protected def transform[A](request: SecuredEnvRequest[A]): Future[AppSecuredEnvRequest[A]] = {
      Future.successful(new AppSecuredRequest[EnvType, A](
        messagesApi = controllerComponents.messagesApi,
        identity = request.identity,
        authenticator = request.authenticator,
        request = request
      ))
    }
  }

  /**
   * Transforms from a UserAwareRequest into AppUserAwareRequest.
   */
  class AppUserAwareActionTransformer extends AbstractActionTransformer[UserAwareEnvRequest, AppUserAwareEnvRequest] {
    override protected def transform[A](request: UserAwareEnvRequest[A]): Future[AppUserAwareEnvRequest[A]] = {
      Future.successful(new AppUserAwareRequest[EnvType, A](
        messagesApi = controllerComponents.messagesApi,
        identity = request.identity,
        authenticator = request.authenticator,
        request = request
      ))
    }
  }
  private val appActionTransformer = new AppActionTransformer
  private val appSecuredActionTransformer = new AppSecuredActionTransformer
  private val appUserAwareActionTransformer = new AppUserAwareActionTransformer

  def UnsecuredAction: ActionBuilder[AppRequest, AnyContent] = silhouette.UnsecuredAction.andThen(appActionTransformer)

  def SecuredAction: ActionBuilder[AppSecuredEnvRequest, AnyContent] = {
    silhouette.SecuredAction.andThen(appSecuredActionTransformer)
  }

  def SecuredAction(errorHandler: SecuredErrorHandler): ActionBuilder[AppSecuredEnvRequest, AnyContent] = {
    silhouette.SecuredAction(errorHandler).andThen(appSecuredActionTransformer)
  }

  def SecuredAction(authorization: Authorization[EnvType#I, EnvType#A]): ActionBuilder[AppSecuredEnvRequest, AnyContent] = {
    silhouette.SecuredAction(authorization).andThen(appSecuredActionTransformer)
  }

  def UserAwareAction: ActionBuilder[AppUserAwareEnvRequest, AnyContent] = silhouette.UserAwareAction.andThen(appUserAwareActionTransformer)

  def userService: UserService = controllerComponents.userService
  def authInfoRepository: AuthInfoRepository = controllerComponents.authInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry = controllerComponents.passwordHasherRegistry
  def authTokenService: AuthTokenService = controllerComponents.authTokenService
  def mailerClient: MailerClient = controllerComponents.mailerClient
  def rememberMeConfig: RememberMeConfig = controllerComponents.rememberMeConfig
  def clock: Clock = controllerComponents.clock
  def credentialsProvider: CredentialsProvider = controllerComponents.credentialsProvider
  def socialProviderRegistry: SocialProviderRegistry = controllerComponents.socialProviderRegistry
  def totpProvider: GoogleTotpProvider = controllerComponents.totpProvider
  def avatarService: AvatarService = controllerComponents.avatarService

  def silhouette: Silhouette[EnvType] = controllerComponents.silhouette
  def authenticatorService: AuthenticatorService[EnvType#A] = silhouette.env.authenticatorService
  def eventBus: EventBus = silhouette.env.eventBus
}

trait SilhouetteComponents[E <: Env] {
  type EnvType = E

  def silhouette: Silhouette[E]
  def userService: UserService
  def authInfoRepository: AuthInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry
  def authTokenService: AuthTokenService
  def mailerClient: MailerClient
  def rememberMeConfig: RememberMeConfig
  def clock: Clock
  def credentialsProvider: CredentialsProvider
  def socialProviderRegistry: SocialProviderRegistry
  def totpProvider: GoogleTotpProvider
  def avatarService: AvatarService
}

trait SilhouetteControllerComponents[E <: Env] extends MessagesControllerComponents with SilhouetteComponents[E]

final case class DefaultSilhouetteControllerComponents[E <: Env] @Inject() (

  silhouette: Silhouette[E],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: AuthTokenService,
  mailerClient: MailerClient,
  rememberMeConfig: RememberMeConfig,
  clock: Clock,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  totpProvider: GoogleTotpProvider,
  avatarService: AvatarService,
  messagesActionBuilder: MessagesActionBuilder,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: scala.concurrent.ExecutionContext
) extends SilhouetteControllerComponents[E]

trait RememberMeConfig {
  def expiry: FiniteDuration
  def idleTimeout: Option[FiniteDuration]
  def cookieMaxAge: Option[FiniteDuration]
}

final case class DefaultRememberMeConfig(
  expiry: FiniteDuration,
  idleTimeout: Option[FiniteDuration],
  cookieMaxAge: Option[FiniteDuration])
  extends RememberMeConfig
