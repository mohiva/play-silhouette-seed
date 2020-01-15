package controllers

import com.mohiva.play.silhouette.api.Env
import org.slf4j.Marker
import play.api.MarkerContext
import play.api.i18n.MessagesApi
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

// XXX should be OOTB
trait SecuredRequestHeader[E <: Env] {
  def identity: E#I
  def authenticator: E#A
}

// XXX should be OOTB
trait UserAwareRequestHeader[E <: Env] {
  def identity: Option[E#I]
  def authenticator: Option[E#A]
}

/**
 * Defines our own request with extended features above and beyond what Silhouette provides.
 */
trait MyRequestHeader extends MessagesRequestHeader
  with PreferredMessagesProvider
  with MarkerContext

/**
 * The request implementation with a parsed body (only relevant for POST requests)
 *
 * @param request the original request
 * @param messagesApi the messages API, needed for producing a Messages instance
 * @tparam B the type of the body, if any.
 */
class MyRequest[B](
  request: Request[B],
  messagesApi: MessagesApi
) extends MessagesRequest[B](request, messagesApi) with MyRequestHeader {
  // Stubbed out here, but see marker context docs
  // https://www.playframework.com/documentation/2.8.x/ScalaLogging#Using-Markers-and-Marker-Contexts
  def marker: Option[Marker] = None
}

/**
 * A request with identity and authenticator traits.
 */
trait MySecuredRequestHeader extends MyRequestHeader
  with SecuredRequestHeader[DefaultEnv]

/**
 * Implementation of secured request.
 */
class MySecuredRequest[B](
  request: Request[B],
  messagesApi: MessagesApi,
  val identity: DefaultEnv#I,
  val authenticator: DefaultEnv#A,
) extends MyRequest(request, messagesApi) with MySecuredRequestHeader

/**
 * A request with optional identity and authenticator traits.
 */
trait MyUserAwareRequestHeader extends MyRequestHeader
  with UserAwareRequestHeader[DefaultEnv]

class MyUserAwareRequest[B](
  request: Request[B],
  messagesApi: MessagesApi,
  val identity: Option[DefaultEnv#I],
  val authenticator: Option[DefaultEnv#A]
) extends MyRequest(request, messagesApi) with MyUserAwareRequestHeader

/**
 * Abstract class to stop defining executionContext in every subclass
 *
 * @param cc controller components
 */
protected abstract class AbstractActionTransformer[-R[_], +P[_]](cc: SilhouetteControllerComponents) extends ActionTransformer[R, P] {
  override protected def executionContext: ExecutionContext =
    cc.executionContext
}

/**
 * Transforms from a Request into MyRequest.
 *
 * @param cc controller components
 */
class MyActionTransformer(cc: SilhouetteControllerComponents) extends AbstractActionTransformer[Request, MyRequest](cc) {
  override protected def transform[A](request: Request[A]): Future[MyRequest[A]] = {
    Future.successful(new MyRequest[A](
      messagesApi = cc.messagesApi,
      request = request
    ))
  }
}

/**
 * Transforms from a SecuredRequest[DefaultEnv] into MySecuredRequest.
 *
 * @param cc controller components
 */
class MySecuredActionTransformer(cc: SilhouetteControllerComponents) extends AbstractActionTransformer[SecuredEnvRequest, MySecuredRequest](cc) {
  override protected def transform[A](request: SecuredEnvRequest[A]): Future[MySecuredRequest[A]] = {
    Future.successful(new MySecuredRequest[A](
      messagesApi = cc.messagesApi,
      identity = request.identity,
      authenticator = request.authenticator,
      request = request
    ))
  }
}

/**
 * Transforms from a UserAwareRequest[DefaultEnv] into MyUserAwareRequest.
 *
 * @param cc controller components
 */
class MyUserAwareActionTransformer(cc: SilhouetteControllerComponents) extends AbstractActionTransformer[UserAwareEnvRequest, MyUserAwareRequest](cc) {
  override protected def transform[A](request: UserAwareEnvRequest[A]): Future[MyUserAwareRequest[A]] = {
    Future.successful(new MyUserAwareRequest[A](
      messagesApi = cc.messagesApi,
      identity = request.identity,
      authenticator = request.authenticator,
      request = request
    ))
  }
}