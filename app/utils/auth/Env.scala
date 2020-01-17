package utils.auth

import com.mohiva.play.silhouette.api.{ Authenticator, Env, Identity }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User

/**
 * The default env.
 */
trait DefaultEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}

trait IdentityProvider[I <: Identity] {
  def identity: I
}

trait IdentityAwareProvider[I <: Identity] {
  def identity: Option[I]
}

trait AuthenticatorProvider[A <: Authenticator] {
  def authenticator: A
}

trait AuthenticatorAwareProvider[A <: Authenticator] {
  def authenticator: Option[A]
}
