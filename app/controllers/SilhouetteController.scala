package controllers

import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorService, AvatarService }
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasherRegistry }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, GoogleTotpProvider, SocialProviderRegistry }
import javax.inject.Inject
import models.services.{ AuthTokenService, UserService }
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{ Langs, MessagesApi }
import play.api.libs.mailer.MailerClient
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.duration.FiniteDuration

abstract class SilhouetteController(override protected val controllerComponents: SilhouetteControllerComponents)
  extends MessagesAbstractController(controllerComponents) with SilhouetteComponents with Logging {

  private val myActionTransformer = new MyActionTransformer(controllerComponents)
  private val mySecuredActionTransformer = new MySecuredActionTransformer(controllerComponents)
  private val myUserAwareActionTransformer = new MyUserAwareActionTransformer(controllerComponents)

  def UnsecuredAction: ActionBuilder[MyRequest, AnyContent] = controllerComponents.silhouette.UnsecuredAction.andThen(myActionTransformer)

  def SecuredAction: ActionBuilder[MySecuredRequest, AnyContent] = {
    controllerComponents.silhouette.SecuredAction.andThen(mySecuredActionTransformer)
  }

  def SecuredAction(errorHandler: SecuredErrorHandler): ActionBuilder[MySecuredRequest, AnyContent] = {
    controllerComponents.silhouette.SecuredAction(errorHandler).andThen(mySecuredActionTransformer)
  }

  def SecuredAction(authorization: Authorization[DefaultEnv#I, DefaultEnv#A]): ActionBuilder[MySecuredRequest, AnyContent] = {
    controllerComponents.silhouette.SecuredAction(authorization).andThen(mySecuredActionTransformer)
  }

  def UserAwareAction: ActionBuilder[MyUserAwareRequest, AnyContent] = controllerComponents.silhouette.UserAwareAction.andThen(myUserAwareActionTransformer)

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

  def silhouette: Silhouette[DefaultEnv] = controllerComponents.silhouette
  def authenticatorService: AuthenticatorService[DefaultEnv#A] = silhouette.env.authenticatorService
  def eventBus: EventBus = silhouette.env.eventBus
}

trait SilhouetteComponents {
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

  def silhouette: Silhouette[DefaultEnv]
}

trait SilhouetteControllerComponents extends MessagesControllerComponents with SilhouetteComponents

final case class DefaultSilhouetteControllerComponents @Inject() (
  silhouette: Silhouette[DefaultEnv],
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
) extends SilhouetteControllerComponents

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
