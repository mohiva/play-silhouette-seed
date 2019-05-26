package models

import java.util.UUID

import org.joda.time.DateTime

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param id The unique token ID.
 * @param userID The unique ID of the user the token is associated with.
 * @param expiry The date-time the token expires.
 */
@deprecated("Remove as soon as possible", "use instead the corresponding models.generated.Tables.AuthTokenRow")
case class AuthToken(
  id: UUID,
  userID: Long,
  expiry: DateTime)
