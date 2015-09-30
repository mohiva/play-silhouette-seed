package models.services

import models.TokenUser
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class TokenServiceImpl extends TokenService[TokenUser] {
  def create (token: TokenUser): Future[Option[TokenUser]] = {
    TokenUser.save(token).map(Some(_))
  }
  def retrieve (id: String): Future[Option[TokenUser]] = {
    TokenUser.findById(id)
  }
  def consume (id: String): Unit = {
    TokenUser.delete(id)
  }
}
