package forms

import play.api.data.Form
import play.api.data.Forms._
import com.mohiva.play.silhouette.core.providers.Credentials

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
