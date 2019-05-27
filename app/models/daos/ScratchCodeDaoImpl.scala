package models.daos

import models.generated.Tables._
import javax.inject._
import models.daos.generic.GenericDaoImpl
import com.mohiva.play.silhouette.api.util.{ PasswordInfo => ExtPasswordInfo }

import scala.concurrent.{ ExecutionContext, Future }
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

/**
 * Concrete ScratchCode DAO implementation.
 */
@Singleton
class ScratchCodeDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[ScratchCode, ScratchCodeRow, Long](dbConfigProvider, ScratchCode) with ScratchCodeDao {
  /**
   * Returns the number of deleted rows, one if successful, zero otherwise.
   * Finds the ScratchCode by userId, hash and password.
   * @param userId the user id.
   * @param target the scratch code to delete.
   * @return the number of deleted rows, one if successful, zero otherwise.
   */
  def delete(userId: Long, target: ExtPasswordInfo): Future[Int] = {
    val action = ScratchCode.filter { scratchCode =>
      scratchCode.userId === userId && scratchCode.hasher === target.hasher && scratchCode.password === target.password
    }.delete
    db.run(action)
  }
}
