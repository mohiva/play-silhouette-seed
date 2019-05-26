package models.daos

import java.util.UUID

import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._
import org.joda.time.DateTime

import scala.concurrent._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

/**
 * Give access to the [[AuthToken]] object.
 */
@Singleton
class AuthTokenDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[AuthToken, AuthTokenRow, Long](dbConfigProvider, AuthToken) with AuthTokenDao {

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  override def find(id: UUID): Future[Option[AuthTokenRow]] = {
    val action = AuthToken.filter(authToken => authToken.tokenId === id.toString).result.headOption
    db.run(action)
  }

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  override def findExpired(dateTime: DateTime): Future[Seq[AuthTokenRow]] = {
    /**
     *
     * tokens.filter {
     * case (_, token) =>
     * token.expiry.isBefore(dateTime)
     * }.values.toSeq
     *
     * val action = sql"""
     * SELECT t1.*
     * FROM "#${AuthToken.baseTableRow.tableName}" t1
     * WHERE t1.active=true
     * AND EXISTS (SELECT * FROM #${LinkedAccount.baseTableRow.tableName} t2
     * WHERE t2.user_id=t1.id
     * AND t2.provider_key=$providerKey
     * AND t2.provider_user_id=$providerUserId)
     * """.as[UserRow].headOption
     *
     */
    //val action = AuthToken.filter(authToken => authToken.expiry.before(dateTime)).result
    //db.run(action)
    ???
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  override def remove(id: UUID): Future[Int] = {
    val action = AuthToken.filter(authToken => authToken.tokenId === id.toString).delete
    db.run(action)
  }
}