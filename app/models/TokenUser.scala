package models

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

case class TokenUser (id: String, email: String, expirationTime: DateTime, isSignUp: Boolean,
                      firstName: String, lastName: String) extends Token {
  def isExpired: Boolean = expirationTime.isBeforeNow
}

object TokenUser {

  private val hoursTillExpiry = 24

  def apply (email: String, isSignUp: Boolean, firstName: String, lastName: String): TokenUser =
    TokenUser(UUID.randomUUID().toString, email, (new DateTime()).plusHours(hoursTillExpiry), isSignUp, firstName, lastName)

  def apply (email: String): TokenUser =
    TokenUser(UUID.randomUUID().toString, email, (new DateTime()).plusHours(hoursTillExpiry), false, "", "") //naughty!

  val tokens = scala.collection.mutable.HashMap[String, TokenUser]()

  def findById (id: String): Future[Option[TokenUser]] = {
    Future.successful(tokens.get(id))
  }

  def save (token: TokenUser): Future[TokenUser] = {
    tokens += (token.id -> token)

    Future.successful(token)
  }

  def delete (id: String): Unit = {
    tokens.remove(id)
  }
}
