package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.generated.Tables.UserRow

/**
 * The default env.
 */
trait DefaultEnv extends Env {
  type I = UserRow
  type A = CookieAuthenticator
}
