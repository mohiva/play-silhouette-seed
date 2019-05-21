package models.daos.generic

import models.generated.Tables.profile.api._
import play.api.db.slick._
import slick.jdbc.JdbcProfile
import slick.lifted.CanBeQueryCondition

import scala.concurrent._

/**
 * Generic DAO definition
 */
trait GenericDao[T <: Table[E] with IdentifyableTable[PK], E <: Entity[PK], PK] extends HasDatabaseConfigProvider[JdbcProfile] {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
   * Returns the row count for this Model
   * @return the row count for this Model
   */
  def count(): Future[Int]

  //------------------------------------------------------------------------
  /**
   * Returns the matching entity for the given id
   * @param id identifier
   * @return the matching entity for the given id
   */
  def findById(id: PK): Future[Option[E]]

  //------------------------------------------------------------------------
  /**
   * Returns all entities in this model
   * @return all entities in this model
   */
  def findAll(): Future[Seq[E]]

  //------------------------------------------------------------------------
  /**
   * Returns entities that satisfy the filter expression.
   * @param expr input filter expression
   * @param wt
   * @tparam C
   * @return entities that satisfy the filter expression.
   */
  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Seq[E]]

  //------------------------------------------------------------------------
  /**
   * Creates (and forgets) a new entity, returns a unit future
   * @param entity entity to create, input id is ignored
   * @return returns a unit future
   */
  def create(entity: E): Future[Unit]

  //------------------------------------------------------------------------
  /**
   * Returns number of inserted entities
   * @param entities to be inserted
   * @return number of inserted entities
   */
  def create(entities: Seq[E]): Future[Unit]

  //------------------------------------------------------------------------
  /**
   * Updates the given entity and returns a Future
   * @param update Entity to update (by id)
   * @return returns a Future
   */
  def update(update: E): Future[Unit]

  //------------------------------------------------------------------------
  /**
   * Deletes the given entity by Id and returns a Future
   * @param id The Id to delete
   * @return returns a Future
   */
  def delete(id: PK): Future[Unit]

  //------------------------------------------------------------------------
  /**
   * Deletes all entities and returns a Future
   * @return returns a Future
   */
  def deleteAll: Future[Unit]
}