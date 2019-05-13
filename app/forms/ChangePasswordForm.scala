package forms

import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

/**
 * The `Change Password` form.
 */
object ChangePasswordForm {
  /**
   * A play framework form.
   */
  def form(implicit messages: Messages) = Form(
    mapping(
      "currentPassword" -> nonEmptyText,
      "newPassword" -> nonEmptyText(minLength = 5),
      "repeatPassword" -> nonEmptyText(minLength = 5)
    )(Data.apply)(Data.unapply).
      verifying(
        messages("sign.up.error.passwords.not.same"),
        data => (data.newPassword != null) && data.newPassword.equals(data.repeatPassword))
  )

  /**
   * The form data.
   * @param currentPassword The current user password.
   * @param newPassword The new user password.
   * @param repeatPassword The password repeat.
   */
  case class Data(
    currentPassword: String,
    newPassword: String,
    repeatPassword: String)
}
