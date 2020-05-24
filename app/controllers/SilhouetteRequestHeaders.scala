package controllers

import com.mohiva.play.silhouette.api._
import play.api.mvc.RequestHeader
import utils.auth._

// XXX should be OOTB
trait SecuredRequestHeader[E <: Env] extends RequestHeader
  with IdentityProvider[E#I]
  with AuthenticatorProvider[E#A]

// XXX should be OOTB
trait UserAwareRequestHeader[E <: Env] extends RequestHeader
  with IdentityAwareProvider[E#I]
  with AuthenticatorAwareProvider[E#A]
