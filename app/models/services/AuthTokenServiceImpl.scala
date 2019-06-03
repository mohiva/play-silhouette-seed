package models.services

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api.util.Clock
import models.daos.AuthTokenDao
import models.generated.Tables.AuthTokenRow
import org.joda.time.DateTimeZone

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 *
 * @param authTokenDao The auth token DAO implementation.
 * @param clock The clock instance.
 * @param ec The execution context.
 */
class AuthTokenServiceImpl @Inject() (
  authTokenDao: AuthTokenDao,
  clock: Clock
)(
  implicit
  ec: ExecutionContext
) extends AuthTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userId The user ID for which the token should be created.
   * @param duration The duration a token expires.
   * @return The saved auth token.
   */
  def create(userId: Long, duration: FiniteDuration = 5 minutes) = {
    val expiry = clock.now.withZone(DateTimeZone.UTC).plusSeconds(duration.toSeconds.toInt)
    val token = AuthTokenRow(userId, UUID.randomUUID().toString, expiry)
    authTokenDao.create(token).map(inserted => if (inserted == 1) token else None.asInstanceOf[AuthTokenRow])
  }

  /**
   * Validates a token ID.
   *
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(id: UUID) = authTokenDao.find(id)

  /**
   * Cleans expired tokens.
   *
   * @return The list of deleted tokens.
   */
  def clean = authTokenDao.findExpired(clock.now.withZone(DateTimeZone.UTC)).flatMap { tokens =>
    Future.sequence(tokens.map { token =>
      authTokenDao.delete(token.id).map(_ => token)
    })
  }
}
