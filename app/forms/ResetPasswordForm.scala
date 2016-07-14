package forms

import play.api.data.Forms._
import play.api.data._

/**
 * The `Reset Password` form.
 */
object ResetPasswordForm {

  /**
   * A play framework form.
   */
  val form = Form(
    "password" -> nonEmptyText
  )
}
