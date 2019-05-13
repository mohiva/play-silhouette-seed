package forms

import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

/**
 * The `Reset Password` form.
 */
object ResetPasswordForm {
  /**
   * A play framework form.
   */
  def form(implicit messages: Messages) = Form(
    mapping(
      "password" -> nonEmptyText(minLength = 5),
      "repeatPassword" -> nonEmptyText(minLength = 5)
    )(Data.apply)(Data.unapply).
      verifying(
        messages("sign.up.error.passwords.not.same"),
        data => (data.password != null) && data.password.equals(data.repeatPassword))
  )

  /**
   * The form data.
   * @param password The password of the user.
   * @param repeatPassword The password repeat.
   */
  case class Data(
    password: String,
    repeatPassword: String)
}
