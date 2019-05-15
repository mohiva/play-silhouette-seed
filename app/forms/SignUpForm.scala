package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

object SignUpForm {
  /**
   * A play framework form.
   */
  def form(implicit messages: Messages) = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5),
      "repeatPassword" -> nonEmptyText(minLength = 5)
    )(Data.apply)(Data.unapply).
      verifying(
        messages("sign.up.error.passwords.not.same"),
        data => (data.password != null) && data.password.equals(data.repeatPassword))
  )

  /**
   * The form data.
   * @param firstName The first name of a user.
   * @param lastName The last name of a user.
   * @param email The email of the user.
   * @param password The password of the user.
   * @param repeatPassword The password repeat.
   */
  case class Data(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    repeatPassword: String)
}