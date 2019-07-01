package forms

import java.util.UUID

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of the credentials plus verification code for TOTP-authentication
 */
object TotpForm {
  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "userID" -> uuid,
      "sharedKey" -> nonEmptyText,
      "rememberMe" -> boolean,
      "verificationCode" -> nonEmptyText(minLength = 6, maxLength = 6)
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   * @param userID The unique identifier of the user.
   * @param sharedKey the TOTP shared key
   * @param rememberMe Indicates if the user should stay logged in on the next visit.
   * @param verificationCode Verification code for TOTP-authentication
   */
  case class Data(
    userID: UUID,
    sharedKey: String,
    rememberMe: Boolean,
    verificationCode: String = "")
}
