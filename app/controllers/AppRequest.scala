package controllers

import com.mohiva.play.silhouette.api.Env
import org.slf4j.Marker
import play.api.MarkerContext
import play.api.i18n.MessagesApi
import play.api.mvc._

import scala.language.higherKinds

/**
 * Defines our own request with extended features above and beyond what Silhouette provides.
 */
trait AppRequestHeader extends MessagesRequestHeader
  with PreferredMessagesProvider
  with MarkerContext

/**
 * The request implementation with a parsed body (only relevant for POST requests)
 *
 * @param request the original request
 * @param messagesApi the messages API, needed for producing a Messages instance
 * @tparam B the type of the body, if any.
 */
class AppRequest[B](
  request: Request[B],
  messagesApi: MessagesApi
) extends MessagesRequest[B](request, messagesApi) with AppRequestHeader {
  // Stubbed out here, but see marker context docs
  // https://www.playframework.com/documentation/2.8.x/ScalaLogging#Using-Markers-and-Marker-Contexts
  def marker: Option[Marker] = None
}

/**
 * A request with identity and authenticator traits.
 */
trait AppSecuredRequestHeader[E <: Env] extends AppRequestHeader
  with SecuredRequestHeader[E]

/**
 * Implementation of secured request.
 */
class AppSecuredRequest[E <: Env, B](
  request: Request[B],
  messagesApi: MessagesApi,
  val identity: E#I,
  val authenticator: E#A,
) extends AppRequest(request, messagesApi) with AppSecuredRequestHeader[E]

/**
 * A request with optional identity and authenticator traits.
 */
trait AppUserAwareRequestHeader[E <: Env] extends AppRequestHeader
  with UserAwareRequestHeader[E]

class AppUserAwareRequest[E <: Env, B](
  request: Request[B],
  messagesApi: MessagesApi,
  val identity: Option[E#I],
  val authenticator: Option[E#A]
) extends AppRequest(request, messagesApi) with AppUserAwareRequestHeader[E]
