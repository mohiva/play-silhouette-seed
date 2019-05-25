package models.daos.generic

import models.generated.Tables.profile.api._
import play.api.db.slick._
import scala.concurrent._

/**
 * Generic Strong DAO implementation
 */
abstract class GenericDaoAutoIncImpl[T <: Table[E] with IdentifyableTable[PK], E <: EntityAutoInc[PK, E], PK: BaseColumnType](dbConfigProvider: DatabaseConfigProvider, tableQuery: TableQuery[T])(implicit ec: ExecutionContext) extends GenericDaoImpl[T, E, PK](dbConfigProvider, tableQuery) with GenericDaoAutoInc[T, E, PK] {
  import shapeless._
  import tag.@@

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
   * Returns newly created entity with updated id
   * @param entity entity to create, input id is ignored
   * @return newly created entity with updated id
   */
  override def createAndFetch(entity: E)(implicit mkLens: MkFieldLens.Aux[E, Symbol @@ Witness.`"id"`.T, PK]): Future[E] = {
    val insertQuery = tableQuery returning tableQuery.map(_.id) into ((row, id) => row.copyWithNewId(id))
    val action = (insertQuery += entity).flatMap(row => tableQuery.filter(_.id === row.id).result.head)
    db.run(action)
  }
}
