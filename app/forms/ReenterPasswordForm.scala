package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of the credentials.
 */
object ReenterPasswordForm {
  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5)
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   * @param email The email of the user.
   * @param password The password of the user.
   */
  case class Data(
    email: String,
    password: String)
}
