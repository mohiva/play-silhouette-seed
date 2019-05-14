package action

import com.mohiva.play.silhouette.api._
import constants.SessionKeys
import play.api.mvc._
import models.User
import scala.concurrent.Future

case class SudoAccessAuthorization[A <: Authenticator]() extends Authorization[User, A] {
  def isAuthorized[B](user: User, authenticator: A)(implicit request: Request[B]) = {
    Future.successful {
      request.session.get(SessionKeys.HAS_SUDO_ACCESS) match {
        case Some(value) => value.toBoolean
        case _ => false
      }
    }
  }
}
