package models.daos.generic

/**
 * Identifyable table for all Table types
 * @tparam PK Primary key type
 */
trait IdentifyableTable[PK] {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def id: slick.lifted.Rep[PK]
}