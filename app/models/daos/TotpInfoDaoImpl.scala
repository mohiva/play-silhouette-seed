package models.daos

import com.mohiva.play.silhouette.impl.providers.{ GoogleTotpInfo => ExtGoogleTotpInfo }
import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import com.mohiva.play.silhouette.persistence.daos.{ AuthInfoDAO, DelegableAuthInfoDAO }
import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._
import models.generated.Tables.profile.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio.DBIOAction
import utils.DaoUtil

import scala.concurrent._
import scala.reflect.ClassTag

@Singleton
class GoogleTotpInfoDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[GoogleTotpInfo, GoogleTotpInfoRow, Long](dbConfigProvider, GoogleTotpInfo) with DelegableAuthInfoDAO[ExtGoogleTotpInfo] with DaoUtil {

  override val classTag = scala.reflect.classTag[ExtGoogleTotpInfo]

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param extLoginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(extLoginInfo: ExtLoginInfo): Future[Option[ExtGoogleTotpInfo]] = {
    val action = (for {
      (loginInfo, scratchCode) <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.joinLeft(ScratchCode).on(_.userId === _.userId)
      totpInfo <- GoogleTotpInfo if totpInfo.userId === loginInfo.userId
    } yield (totpInfo, scratchCode)).result
    simplify(db.run(action)).map {
      case Some((totpInfoRow, scratchCodes)) => Some(totpInfoRow.toExt(scratchCodes.map(_.toExt)))
      case _ => None
    }
  }

  /**
   * Returns the inserted `totpInfo` instance including the hashed scratch codes. We first
   * look up the `LoginInfo` by the relevant search criteria, fetching its `userId`
   * which is then used to persist a `GoogleTotpInfo` and multiple `ScratchCode`.
   *
   * @param extLoginInfo The login info for which the auth info should be added.
   * @param extGoogleTotpInfo The TOTP info to add containing the scratch codes.
   * @return the inserted `totpInfo` instance including the hashed scratch codes.
   */
  def add(extLoginInfo: ExtLoginInfo, extGoogleTotpInfo: ExtGoogleTotpInfo): Future[ExtGoogleTotpInfo] = {
    val insertion = (for {
      userId <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.map(_.userId).result.head
      _ <- (GoogleTotpInfo += GoogleTotpInfoRow(userId, extGoogleTotpInfo.sharedKey))
      _ <- DBIOAction.sequence(extGoogleTotpInfo.scratchCodes.map { scratchCode => ScratchCode += (ScratchCodeRow(userId, scratchCode.hasher, scratchCode.password, scratchCode.salt)) })
    } yield ()).transactionally
    db.run(insertion).map(_ => extGoogleTotpInfo)
  }

  /**
   * Returns the updated [[GoogleTotpInfo]] ensuring also deletion of the used scratch code.
   *
   * @param extLoginInfo The login info for which the auth info should be updated.
   * @param extGoogleTotpInfo The auth info to update.
   * @return the updated [[GoogleTotpInfo]] ensuring also deletion of the used scratch code.
   */
  def update(extLoginInfo: ExtLoginInfo, extGoogleTotpInfo: ExtGoogleTotpInfo): Future[ExtGoogleTotpInfo] = {
    // TODO: implement and then get rid of the unnecessary ScratchCodeDaoImpl
    Future.successful(extGoogleTotpInfo)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param extLoginInfo The login info for which the auth info should be saved.
   * @param extGoogleTotpInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(extLoginInfo: ExtLoginInfo, extGoogleTotpInfo: ExtGoogleTotpInfo): Future[ExtGoogleTotpInfo] = {
    find(extLoginInfo).flatMap {
      case Some(_) => update(extLoginInfo, extGoogleTotpInfo)
      case None => add(extLoginInfo, extGoogleTotpInfo)
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
      _ <- GoogleTotpInfo.filter(_.userId === userId).delete
      _ <- ScratchCode.filter(_.userId === userId).delete
    } yield ()).transactionally
    db.run(action)
  }
}