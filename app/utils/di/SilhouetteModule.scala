package utils.di

import play.api.Play
import play.api.Play.current
import play.Logger
import com.google.inject.{ Provides, AbstractModule }
import net.codingwell.scalaguice.ScalaModule
import com.mohiva.play.silhouette.core.{EventBus, Environment}
import com.mohiva.play.silhouette.core.utils._
import com.mohiva.play.silhouette.core.services._
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.providers.oauth2._
import com.mohiva.play.silhouette.core.providers.oauth1._
import com.mohiva.play.silhouette.contrib.utils._
import com.mohiva.play.silhouette.contrib.services._
import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import models.services.{UserService, UserServiceImpl}
import models.daos._
import models.daos.slick._
import models.User

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure() {
    bind[UserService].to[UserServiceImpl]
    val useSlick = Play.configuration.getBoolean("silhouette.seed.db.useSlick").getOrElse(false)
    if (useSlick) {
      Logger.debug("Binding to Slick DAO implementations.")
      bind[UserDAO].to[UserDAOSlick]
      bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAOSlick]
      bind[DelegableAuthInfoDAO[OAuth1Info]].to[OAuth1InfoDAOSlick]
      bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAOSlick]
    } else {
      Logger.debug("Binding to In-Memory DAO implementations.")
      bind[UserDAO].to[UserDAOImpl]
      bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]
      bind[DelegableAuthInfoDAO[OAuth1Info]].to[OAuth1InfoDAO]
      bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAO]
    }
    bind[CacheLayer].to[PlayCacheLayer]
    bind[HTTPLayer].to[PlayHTTPLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[EventBus].toInstance(EventBus())
  }

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[CachedCookieAuthenticator],
    eventBus: EventBus,
    credentialsProvider: CredentialsProvider,
    facebookProvider: FacebookProvider,
    googleProvider: GoogleProvider,
    twitterProvider: TwitterProvider): Environment[User, CachedCookieAuthenticator] = {

    Environment[User, CachedCookieAuthenticator](
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
   * @param cacheLayer The cache layer implementation.
   * @param idGenerator The ID generator used to create the authenticator ID.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    cacheLayer: CacheLayer,
    idGenerator: IDGenerator): AuthenticatorService[CachedCookieAuthenticator] = {

    new CachedCookieAuthenticatorService(CachedCookieAuthenticatorSettings(
      cookieName = Play.configuration.getString("silhouette.authenticator.cookieName").get,
      cookiePath = Play.configuration.getString("silhouette.authenticator.cookiePath").get,
      cookieDomain = Play.configuration.getString("silhouette.authenticator.cookieDomain"),
      secureCookie = Play.configuration.getBoolean("silhouette.authenticator.secureCookie").get,
      httpOnlyCookie = Play.configuration.getBoolean("silhouette.authenticator.httpOnlyCookie").get,
      cookieIdleTimeout = Play.configuration.getInt("silhouette.authenticator.cookieIdleTimeout").get,
      cookieAbsoluteTimeout = Play.configuration.getInt("silhouette.authenticator.cookieAbsoluteTimeout"),
      authenticatorExpiry = Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").get
    ), cacheLayer, idGenerator, Clock())
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
   * @param cacheLayer The cache layer implementation.
   * @param httpLayer The HTTP layer implementation.
   * @return The Facebook provider.
   */
  @Provides
  def provideFacebookProvider(cacheLayer: CacheLayer, httpLayer: HTTPLayer): FacebookProvider = {
    FacebookProvider(cacheLayer, httpLayer, OAuth2Settings(
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
   * @param cacheLayer The cache layer implementation.
   * @param httpLayer The HTTP layer implementation.
   * @return The Google provider.
   */
  @Provides
  def provideGoogleProvider(cacheLayer: CacheLayer, httpLayer: HTTPLayer): GoogleProvider = {
    GoogleProvider(cacheLayer, httpLayer, OAuth2Settings(
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

    TwitterProvider(cacheLayer, httpLayer, new PlayOAuth1Service(settings), settings)
  }
}
