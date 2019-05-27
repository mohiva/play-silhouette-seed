package models.daos

import com.mohiva.play.silhouette.impl.providers.{ TotpInfo => ExtTotpInfo }
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import com.mohiva.play.silhouette.api.util.{ PasswordInfo => ExtPasswordInfo }
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO
import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._
import models.generated.Tables.profile.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio.DBIOAction

import scala.concurrent._

@Singleton
class TotpInfoDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[TotpInfo, TotpInfoRow, Long](dbConfigProvider, TotpInfo) with AuthInfoDAO[ExtTotpInfo] {

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param extLoginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(extLoginInfo: ExtLoginInfo): Future[Option[ExtTotpInfo]] = {
    val action = (for {
      loginInfo <- LoginInfo if loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey
      totpInfo <- TotpInfo if totpInfo.userId === loginInfo.userId
      scratchCode <- ScratchCode if scratchCode.userId === loginInfo.userId
    } yield (totpInfo, scratchCode)).result
    db.run(action).map {
      case results =>
        val scratchCodes = results.map(_._2).map { scratchCode => ExtPasswordInfo(scratchCode.hasher, scratchCode.password, scratchCode.salt) }
        results.headOption.map {
          case (totpInfo, _) => ExtTotpInfo(totpInfo.sharedKey, scratchCodes)
        }
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
   * Updates the auth info for the given login info.
   *
   * @param extLoginInfo The login info for which the auth info should be updated.
   * @param extTotpInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(extLoginInfo: ExtLoginInfo, extTotpInfo: ExtTotpInfo): Future[ExtTotpInfo] = {
    // do nothing as the only update possible is a removal
    // of a scratchCode that's done manually and separately.
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
    val action = LoginInfo.filter { loginInfo =>
      loginInfo.providerId === extLoginInfo.providerID &&
        loginInfo.providerKey === extLoginInfo.providerKey
    }.result.head.map(_.userId).flatMap { userId =>
      TotpInfo.filter(_.userId === userId).delete
      ScratchCode.filter(_.userId === userId).delete
    }.transactionally
    db.run(action).map(() => _)
  }
}