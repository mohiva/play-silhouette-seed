package models.daos.generic

/**
 * Identifyable base for all Model types, it is also a Product
 *
 * @tparam PK Primary key type
 */
trait Entity[PK] {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def id: PK
}