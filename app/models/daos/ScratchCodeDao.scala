package models.daos

import models.daos.generic.GenericDao
import models.generated.Tables.{ ScratchCode, ScratchCodeRow }
import com.mohiva.play.silhouette.api.util.{ PasswordInfo => ExtPasswordInfo }

import scala.concurrent.Future

trait ScratchCodeDao extends GenericDao[ScratchCode, ScratchCodeRow, Long] {
  /**
   * Returns the number of deleted rows, one if successful, zero otherwise.
   * Finds the ScratchCode by userId, hash and password.
   * @param userId the user id.
   * @param target the scratch code to delete.
   * @return the number of deleted rows, one if successful, zero otherwise.
   */
  def delete(userId: Long, target: ExtPasswordInfo): Future[Int]
}
