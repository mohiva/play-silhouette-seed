package action

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import constants.SessionKeys
import play.api.mvc._
import models.generated.Tables.UserRow
import models.services.UserService

import scala.concurrent.ExecutionContext

case class WithSudoAccess[A <: Authenticator]()(implicit userService: UserService, ec: ExecutionContext) extends Authorization[UserRow, A] {
  import UserService._

  def isAuthorized[B](user: UserRow, authenticator: A)(implicit request: Request[B]) = {
    user.loginInfo.map {
      case Some(loginInfo) => {
        (loginInfo.providerID != CredentialsProvider.ID) ||
          (request.session.get(SessionKeys.HAS_SUDO_ACCESS) match {
            case Some(hasSudoAccess) => (loginInfo.providerID != CredentialsProvider.ID) || hasSudoAccess.toBoolean
            case _ => false
          })
      }
      case None => false
    }
  }
}
