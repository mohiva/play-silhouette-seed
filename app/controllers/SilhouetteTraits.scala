package controllers

import com.mohiva.play.silhouette.api.{Authenticator, Env, Identity}
import play.api.mvc.RequestHeader

trait IdentityProvider[I <: Identity] {
  def identity: I
}

trait AuthenticatorProvider[A <: Authenticator] {
  def authenticator: A
}

// XXX should be OOTB
trait SecuredRequestHeader[E <: Env] extends RequestHeader
  with IdentityProvider[E#I]
  with AuthenticatorProvider[E#A]

trait IdentityAwareProvider[I <: Identity] {
  def identity: Option[I]
}

trait AuthenticatorAwareProvider[A <: Authenticator] {
  def authenticator: Option[A]
}

// XXX should be OOTB
trait UserAwareRequestHeader[E <: Env] extends RequestHeader
  with IdentityAwareProvider[E#I]
  with AuthenticatorAwareProvider[E#A]
