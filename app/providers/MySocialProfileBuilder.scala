package providers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers._
import org.joda.time.LocalDate

/**
 * The social profile contains all the data returned from the social providers after authentication.
 *
 * Not every provider returns all the data defined in this class. This is also the representation of the
 * most common profile information provided by the social providers. The data can be used to create a new
 * identity for the first authentication(which is also the registration) or to update an existing identity
 * on every subsequent authentication.
 *
 * @param loginInfo The linked login info.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName Maybe the last name of the authenticated user.
 * @param fullName Maybe the full name of the authenticated user.
 * @param birthDate Maybe the birth date of the authenticated user.
 * @param gender Maybe the gender of the authenticated user.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 */
case class MySocialProfile(
  loginInfo: LoginInfo,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  fullName: Option[String] = None,
  birthDate: Option[LocalDate] = None,
  gender: Option[String] = None,
  email: Option[String] = None,
  avatarURL: Option[String] = None) extends SocialProfile

/**
 * The profile builder for this application social.
 */
trait MySocialProfileBuilder extends SocialProfileBuilder {
  self: SocialProvider =>

  /**
   * The type of the profile a profile builder is responsible for.
   */
  override type Profile = MySocialProfile
}
