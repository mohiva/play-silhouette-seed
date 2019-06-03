package models.daos

import com.mohiva.play.silhouette.impl.providers.{ TotpInfo => ExtTotpInfo }
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO
import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._
import models.generated.Tables.profile.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio.DBIOAction
import utils.DaoUtil

import scala.concurrent._

@Singleton
class TotpInfoDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[TotpInfo, TotpInfoRow, Long](dbConfigProvider, TotpInfo) with AuthInfoDAO[ExtTotpInfo] with DaoUtil {

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param extLoginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(extLoginInfo: ExtLoginInfo): Future[Option[ExtTotpInfo]] = {
    val action = (for {
      (loginInfo, scratchCode) <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.joinLeft(ScratchCode).on(_.userId === _.userId)
      totpInfo <- TotpInfo if totpInfo.userId === loginInfo.userId
    } yield (totpInfo, scratchCode)).result
    simplify(db.run(action)).map {
      case Some((totpInfoRow, scratchCodes)) => Some(totpInfoRow.toExt(scratchCodes.map(_.toExt)))
      case _ => None
    }
  }

  /**
   * Returns the inserted `totpInfo` instance including the hashed scratch codes. We first
   * look up the `LoginInfo` by the relevant search criteria, fetching its `userId`
   * which is then used to persist a `TotpInfo` and multiple `ScratchCode`.
   *
   * @param extLoginInfo The login info for which the auth info should be added.
   * @param extTotpInfo The TOTP info to add containing the scratch codes.
   * @return the inserted `totpInfo` instance including the hashed scratch codes.
   */
  def add(extLoginInfo: ExtLoginInfo, extTotpInfo: ExtTotpInfo): Future[ExtTotpInfo] = {
    val insertion = (for {
      userId <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.map(_.userId).result.head
      _ <- (TotpInfo += TotpInfoRow(userId, extTotpInfo.sharedKey))
      _ <- DBIOAction.sequence(extTotpInfo.scratchCodes.map { scratchCode => ScratchCode += (ScratchCodeRow(userId, scratchCode.hasher, scratchCode.password, scratchCode.salt)) })
    } yield ()).transactionally
    db.run(insertion).map(_ => extTotpInfo)
  }

  /**
   * Returns the updated [[TotpInfo]] ensuring also deletion of the used scratch code.
   *
   * @param extLoginInfo The login info for which the auth info should be updated.
   * @param extTotpInfo The auth info to update.
   * @return the updated [[TotpInfo]] ensuring also deletion of the used scratch code.
   */
  def update(extLoginInfo: ExtLoginInfo, extTotpInfo: ExtTotpInfo): Future[ExtTotpInfo] = {
    // TODO: implement and then get rid of the unnecessary ScratchCodeDaoImpl
    Future.successful(extTotpInfo)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param extLoginInfo The login info for which the auth info should be saved.
   * @param extTotpInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(extLoginInfo: ExtLoginInfo, extTotpInfo: ExtTotpInfo): Future[ExtTotpInfo] = {
    find(extLoginInfo).flatMap {
      case Some(_) => update(extLoginInfo, extTotpInfo)
      case None => add(extLoginInfo, extTotpInfo)
    }
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param extLoginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(extLoginInfo: ExtLoginInfo): Future[Unit] = {
    val action = (for {
      userId <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.map(_.userId).result.head
      _ <- TotpInfo.filter(_.userId === userId).delete
      _ <- ScratchCode.filter(_.userId === userId).delete
    } yield ()).transactionally
    db.run(action)
  }
}