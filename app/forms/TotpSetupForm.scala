package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of the form with data for TOTP-authentication enabling
 */
object TotpSetupForm {
  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "sharedKey" -> nonEmptyText,
      "scratchCodes" -> set(nonEmptyText),
      "verificationCode" -> nonEmptyText(minLength = 6, maxLength = 6)
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   * @param sharedKey Shared user key for TOTP authentication.
   * @param scratchCodes Scratch or recovery codes used for one time TOTP authentication.
   * @param verificationCode Verification code for TOTP-authentication
   */
  case class Data(
    sharedKey: String,
    scratchCodes: Set[String],
    verificationCode: String = "")
}
