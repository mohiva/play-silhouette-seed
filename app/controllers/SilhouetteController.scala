package controllers

import com.mohiva.play.silhouette.api.actions.{ SecuredActionBuilder, UnsecuredActionBuilder }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorService, AvatarService }
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasherRegistry }
import com.mohiva.play.silhouette.api.{ EventBus, Silhouette }
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, GoogleTotpProvider, SocialProviderRegistry }
import javax.inject.Inject
import models.services.{ AuthTokenService, UserService }
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{ I18nSupport, Langs, MessagesApi }
import play.api.libs.mailer.MailerClient
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.duration.FiniteDuration

abstract class SilhouetteController(override protected val controllerComponents: SilhouetteControllerComponents)
  extends MessagesAbstractController(controllerComponents) with SilhouetteComponents with I18nSupport with Logging {

  def SecuredAction: SecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.SecuredAction
  def UnsecuredAction: UnsecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.UnsecuredAction

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
  def authenticatorService: AuthenticatorService[AuthType] = silhouette.env.authenticatorService
  def eventBus: EventBus = silhouette.env.eventBus
}

trait SilhouetteComponents {
  type EnvType = DefaultEnv
  type AuthType = EnvType#A
  type IdentityType = EnvType#I

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

  def silhouette: Silhouette[EnvType]
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
