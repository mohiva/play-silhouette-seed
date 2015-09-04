package models.services

import scala.concurrent.Future
import models.Token

/**
 * A trait that provides the means to handle auth tokens for the Silhouette module.
 *
 * Tokens are needed for users that are creating an account in the system instead of using
 * one in a third-party system.
 */
trait TokenService[T <: Token] {

  /**
   * Creates a new token.
   *
   * The new token will be persisted so that later it can be retrieved by its ID.
   *
   * @param token The token to create.
   * @return The created token or None if the token couldn't be created.
   */
  def create(token: T): Future[Option[T]]

  /**
   * Retrieves an available token.
   *
   * @param id The token ID.
   * @return The retrieved token or None if no token could be retrieved for the given ID.
   */
  def retrieve(id: String): Future[Option[T]]

  /**
   * Consumes a token.
   *
   * This method makes the token unavailable for further use.
   * It's up to the implementation how to do that. For example, the token can be deleted,
   * updated as "consumed", moved to another table, etc.
   *
   * Consumed tokens can't be retrieved.
   *
   * @param id The ID of the token to consume.
   */
  def consume(id: String)
}
