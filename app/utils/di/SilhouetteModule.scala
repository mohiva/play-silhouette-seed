package utils.di

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1._
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateProvider, CookieStateSettings}
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import models.User
import models.daos._
import models.services.{UserService, UserServiceImpl}
import net.codingwell.scalaguice.ScalaModule
import play.api.Play
import play.api.Play.current

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[UserDAO].to[UserDAOImpl]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]
    bind[DelegableAuthInfoDAO[OAuth1Info]].to[OAuth1InfoDAO]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAO]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[HTTPLayer].to[PlayHTTPLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
  }

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @param credentialsProvider The credentials provider implementation.
   * @param facebookProvider The Facebook provider implementation.
   * @param googleProvider The Google provider implementation.
   * @param twitterProvider The Twitter provider implementation.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[SessionAuthenticator],
    eventBus: EventBus,
    credentialsProvider: CredentialsProvider,
    facebookProvider: FacebookProvider,
    googleProvider: GoogleProvider,
    twitterProvider: TwitterProvider): Environment[User, SessionAuthenticator] = {

    Environment[User, SessionAuthenticator](
      userService,
      authenticatorService,
      Map(
        credentialsProvider.id -> credentialsProvider,
        facebookProvider.id -> facebookProvider,
        googleProvider.id -> googleProvider,
        twitterProvider.id -> twitterProvider
      ),
      eventBus
    )
  }

  /**
   * Provides the authenticator service.
   *
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    fingerprintGenerator: FingerprintGenerator): AuthenticatorService[SessionAuthenticator] = {
    new SessionAuthenticatorService(SessionAuthenticatorSettings(
      sessionKey = Play.configuration.getString("silhouette.authenticator.sessionKey").get,
      encryptAuthenticator = Play.configuration.getBoolean("silhouette.authenticator.encryptAuthenticator").get,
      useFingerprinting = Play.configuration.getBoolean("silhouette.authenticator.useFingerprinting").get,
      authenticatorIdleTimeout = Play.configuration.getInt("silhouette.authenticator.authenticatorIdleTimeout"),
      authenticatorExpiry = Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").get
    ), fingerprintGenerator, Clock())
  }

  /**
   * Provides the auth info service.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @param oauth1InfoDAO The implementation of the delegable OAuth1 auth info DAO.
   * @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
   * @return The auth info service instance.
   */
  @Provides
  def provideAuthInfoService(
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
    oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
    oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoService = {

    new DelegableAuthInfoService(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO)
  }

  /**
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
   * Provides the OAuth2 state provider.
   *
   * @param idGenerator The ID generator implementation.
   * @return The OAuth2 state provider implementation.
   */
  @Provides
  def provideOAuth2StateProvider(idGenerator: IDGenerator): OAuth2StateProvider = {
    new CookieStateProvider(CookieStateSettings(
      cookieName = Play.configuration.getString("silhouette.oauth2StateProvider.cookieName").get,
      cookiePath = Play.configuration.getString("silhouette.oauth2StateProvider.cookiePath").get,
      cookieDomain = Play.configuration.getString("silhouette.oauth2StateProvider.cookieDomain"),
      secureCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.secureCookie").get,
      httpOnlyCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.httpOnlyCookie").get,
      expirationTime = Play.configuration.getInt("silhouette.oauth2StateProvider.expirationTime").get
    ), idGenerator, Clock())
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoService The auth info service implemenetation.
   * @param passwordHasher The default password hasher implementation.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
    authInfoService: AuthInfoService,
    passwordHasher: PasswordHasher): CredentialsProvider = {

    new CredentialsProvider(authInfoService, passwordHasher, Seq(passwordHasher))
  }

  /**
   * Provides the Facebook provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Facebook provider.
   */
  @Provides
  def provideFacebookProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): FacebookProvider = {
    FacebookProvider(httpLayer, stateProvider, OAuth2Settings(
      authorizationURL = Play.configuration.getString("silhouette.facebook.authorizationURL").get,
      accessTokenURL = Play.configuration.getString("silhouette.facebook.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.facebook.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.facebook.clientID").get,
      clientSecret = Play.configuration.getString("silhouette.facebook.clientSecret").get,
      scope = Play.configuration.getString("silhouette.facebook.scope")))
  }

  /**
   * Provides the Google provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Google provider.
   */
  @Provides
  def provideGoogleProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): GoogleProvider = {
    GoogleProvider(httpLayer, stateProvider, OAuth2Settings(
      authorizationURL = Play.configuration.getString("silhouette.google.authorizationURL").get,
      accessTokenURL = Play.configuration.getString("silhouette.google.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.google.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.google.clientID").get,
      clientSecret = Play.configuration.getString("silhouette.google.clientSecret").get,
      scope = Play.configuration.getString("silhouette.google.scope")))
  }

  /**
   * Provides the Twitter provider.
   *
   * @param cacheLayer The cache layer implementation.
   * @param httpLayer The HTTP layer implementation.
   * @return The Twitter provider.
   */
  @Provides
  def provideTwitterProvider(cacheLayer: CacheLayer, httpLayer: HTTPLayer): TwitterProvider = {
    val settings = OAuth1Settings(
      requestTokenURL = Play.configuration.getString("silhouette.twitter.requestTokenURL").get,
      accessTokenURL = Play.configuration.getString("silhouette.twitter.accessTokenURL").get,
      authorizationURL = Play.configuration.getString("silhouette.twitter.authorizationURL").get,
      callbackURL = Play.configuration.getString("silhouette.twitter.callbackURL").get,
      consumerKey = Play.configuration.getString("silhouette.twitter.consumerKey").get,
      consumerSecret = Play.configuration.getString("silhouette.twitter.consumerSecret").get)

    TwitterProvider(httpLayer, new PlayOAuth1Service(settings), settings)
  }
}
