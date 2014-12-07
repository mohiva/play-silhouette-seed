package forms

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of the credentials.
 */
object SignInForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "identifier" -> email,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply)
  )
}
