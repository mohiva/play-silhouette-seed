package models.daos

import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import com.mohiva.play.silhouette.persistence.daos.{ AuthInfoDAO, DelegableAuthInfoDAO }
import com.mohiva.play.silhouette.api.util.{ PasswordInfo => ExtPasswordInfo }
import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._

import scala.concurrent._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._
import slick.sql.FixedSqlAction

import scala.reflect.ClassTag

@Singleton
class PasswordInfoDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[PasswordInfo, PasswordInfoRow, Long](dbConfigProvider, PasswordInfo) with DelegableAuthInfoDAO[ExtPasswordInfo] {

  override val classTag = scala.reflect.classTag[ExtPasswordInfo]

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param extLoginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(extLoginInfo: ExtLoginInfo): Future[Option[ExtPasswordInfo]] = {
    val action = (for {
      loginInfo <- LoginInfo if loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey
      passwordInfo <- PasswordInfo if passwordInfo.userId === loginInfo.userId
    } yield passwordInfo).result.headOption
    db.run(action).map(_.map(_.toExt))
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param extLoginInfo The login info for which the auth info should be added.
   * @param extPasswordInfo The auth info to add.
   * @return The added auth info.
   */
  def add(extLoginInfo: ExtLoginInfo, extPasswordInfo: ExtPasswordInfo): Future[ExtPasswordInfo] = {
    val insertion = (for {
      userId <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.map(_.userId).result.head
      _ <- (PasswordInfo += PasswordInfoRow(userId, extPasswordInfo.hasher, extPasswordInfo.password, extPasswordInfo.salt))
    } yield ()).transactionally
    db.run(insertion).map(_ => extPasswordInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param extLoginInfo The login info for which the auth info should be updated.
   * @param extPasswordInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(extLoginInfo: ExtLoginInfo, extPasswordInfo: ExtPasswordInfo): Future[ExtPasswordInfo] = {
    db.run(createUpsertTemplate(extLoginInfo, extPasswordInfo, PasswordInfo.update))
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param extLoginInfo The login info for which the auth info should be saved.
   * @param extPasswordInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(extLoginInfo: ExtLoginInfo, extPasswordInfo: ExtPasswordInfo): Future[ExtPasswordInfo] = {
    db.run(createUpsertTemplate(extLoginInfo, extPasswordInfo, PasswordInfo.insertOrUpdate))
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
      _ <- PasswordInfo.filter(_.userId === userId).delete
    } yield ())
    db.run(action).map(_ => ())
  }

  /**
   * Returns reusable `DBIOAction` template for either `update` or `insertOrUpdate`.
   *
   * @param extLoginInfo the Silhouette `LoginInfo` instance
   * @param extPasswordInfo the Silhouette `PasswordInfo` instance
   * @param func the `update` or `insertOrUpdate` function.
   * @return reusable `DBIOAction` template for either `update` or `insertOrUpdate`.
   */
  private def createUpsertTemplate(extLoginInfo: ExtLoginInfo, extPasswordInfo: ExtPasswordInfo, func: (PasswordInfoRow) => FixedSqlAction[Int, NoStream, Effect.Write]) = {
    (for {
      userId <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.map(_.userId).result.head
      extPasswordInfo <- {
        val updated = PasswordInfoRow(userId, extPasswordInfo.hasher, extPasswordInfo.password, extPasswordInfo.salt)
        func(updated).map(_ => extPasswordInfo)
      }
    } yield (extPasswordInfo))
  }
}