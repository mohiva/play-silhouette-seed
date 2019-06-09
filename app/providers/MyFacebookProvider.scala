package providers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import providers.MyFacebookProvider._

import scala.concurrent.Future

/**
 * The profile parser for the common social profile.
 */
class MyFacebookProfileParser extends SocialProfileParser[JsValue, MySocialProfile, OAuth2Info] {
  val dateFormat = "MM/dd/yyyy"

  implicit val jodaDateReads = Reads[LocalDate](js =>
    js.validate[String].map[LocalDate](dtString =>
      LocalDate.parse(dtString, DateTimeFormat.forPattern(dateFormat))
    )
  )

  /**
   * Parses the social profile.
   *
   * @param json     The content returned from the provider.
   * @param authInfo The auth info to query the provider again for additional data.
   * @return The social profile from given result.
   */
  override def parse(json: JsValue, authInfo: OAuth2Info) = Future.successful {
    val userID = (json \ "id").as[String]
    val firstName = (json \ "first_name").asOpt[String]
    val lastName = (json \ "last_name").asOpt[String]
    val fullName = (json \ "name").asOpt[String]
    val birthDate = (json \ "birthday").asOpt[LocalDate]
    val gender = (json \ "gender").asOpt[String]
    val avatarURL = (json \ "picture" \ "data" \ "url").asOpt[String]
    val email = (json \ "email").asOpt[String]

    MySocialProfile(
      loginInfo = LoginInfo(ID, userID),
      firstName = firstName,
      lastName = lastName,
      fullName = fullName,
      birthDate = birthDate,
      gender = gender,
      email = email,
      avatarURL = avatarURL)
  }
}

class MyFacebookProvider(
  protected val httpLayer: HTTPLayer,
  protected val stateHandler: SocialStateHandler,
  val settings: OAuth2Settings)
  extends OAuth2Provider with MySocialProfileBuilder {

  /**
   * The type of this class.
   */
  override type Self = MyFacebookProvider

  /**
   * The content type to parse a profile from.
   */
  override type Content = JsValue

  /**
   * The provider ID.
   */
  override val id = ID

  /**
   * Defines the URLs that are needed to retrieve the profile data.
   */
  override protected val urls = Map("api" -> settings.apiURL.getOrElse(API))

  /**
   * Builds the social profile.
   *
   * @param authInfo The auth info received from the provider.
   * @return On success the build social profile, otherwise a failure.
   */
  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    httpLayer.url(urls("api").format(authInfo.accessToken)).get().flatMap { response =>
      val json = response.json
      (json \ "error").asOpt[JsObject] match {
        case Some(error) =>
          val errorMsg = (error \ "message").as[String]
          val errorType = (error \ "type").as[String]
          val errorCode = (error \ "code").as[Int]

          throw new ProfileRetrievalException(SpecifiedProfileError.format(id, errorMsg, errorType, errorCode))
        case _ => profileParser.parse(json, authInfo)
      }
    }
  }

  /**
   * The profile parser implementation.
   */
  override val profileParser = new MyFacebookProfileParser

  /**
   * Gets a provider initialized with a new settings object.
   *
   * @param f A function which gets the settings passed and returns different settings.
   * @return An instance of the provider initialized with new settings.
   */
  override def withSettings(f: (Settings) => Settings) = new MyFacebookProvider(httpLayer, stateHandler, f(settings))
}

/**
 * The companion object.
 */
object MyFacebookProvider {

  /**
   * The error messages.
   */
  val SpecifiedProfileError = "[Silhouette][%s] Error retrieving profile information. Error message: %s, type: %s, code: %s"

  /**
   * The Facebook constants.
   */
  val ID = "facebook"
  val API = "https://graph.facebook.com/v2.3/me?fields=name,first_name,last_name,birthday,gender,picture,email&return_ssl_resources=1&access_token=%s"
}