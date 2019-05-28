package models.daos

import com.mohiva.play.silhouette.api.{ LoginInfo => ExtLoginInfo }
import com.mohiva.play.silhouette.impl.providers.{ OAuth2Info => ExtOAuth2Info }
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO
import javax.inject._
import models.daos.generic.GenericDaoImpl
import models.generated.Tables._
import models.generated.Tables.profile.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio.DBIOAction

import scala.concurrent._

@Singleton
class OAuth2InfoDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends GenericDaoImpl[OAuth2Info, OAuth2InfoRow, Long](dbConfigProvider, OAuth2Info) with AuthInfoDAO[ExtOAuth2Info] {

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param extLoginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(extLoginInfo: ExtLoginInfo): Future[Option[ExtOAuth2Info]] = {
    val action = (for {
      loginInfo <- LoginInfo if loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey
      oauth2Info <- OAuth2Info if oauth2Info.userId === loginInfo.userId
      oauth2InfoParam <- OAuth2InfoParam if oauth2InfoParam.userId === loginInfo.userId
    } yield (oauth2Info, oauth2InfoParam)).result
    db.run(action).map {
      case results =>
        val params = results.map(_._2).map { param => (param.key -> param.value) } match {
          case seq if (seq.nonEmpty) => Some(seq.toMap)
          case _ => None
        }
        results.headOption.map {
          case (oauth2Info, _) => oauth2Info.toExt(params)
        }
    }
  }

  /**
   * Returns the inserted `oauth2Info` instance including the params. We first
   * look up the `LoginInfo` by the relevant search criteria, fetching its `userId`
   * which is then used to persist a `OAuth2Info` and multiple `OAuth2InfoParam`.
   *
   * @param extLoginInfo The login info for which the auth info should be added.
   * @param extOAuth2Info The TOTP info to add containing the params.
   * @return the inserted `oauth2Info` instance including the params.
   */
  def add(extLoginInfo: ExtLoginInfo, extOAuth2Info: ExtOAuth2Info): Future[ExtOAuth2Info] = {
    val insertion = (for {
      userId <- LoginInfo.filter { loginInfo => loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey }.map(_.userId).result.head
      _ <- (OAuth2Info += OAuth2InfoRow(userId, extOAuth2Info.accessToken, extOAuth2Info.tokenType, extOAuth2Info.expiresIn, extOAuth2Info.refreshToken))
      _ <- extOAuth2Info.params.map { params =>
        DBIOAction.sequence(params.map { param => (OAuth2InfoParam += OAuth2InfoParamRow(userId, param._1, param._2)) })
      }.getOrElse(DBIOAction.seq())
    } yield ()).transactionally
    db.run(insertion).map(_ => extOAuth2Info)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param extLoginInfo The login info for which the auth info should be updated.
   * @param extOAuth2Info The auth info to update.
   * @return The updated auth info.
   */
  def update(extLoginInfo: ExtLoginInfo, extOAuth2Info: ExtOAuth2Info): Future[ExtOAuth2Info] = {
    // TODO: this update doesn't account for removed extOAuth2Info#params
    val action = LoginInfo.filter { loginInfo =>
      loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey
    }.result.head.map(_.userId).flatMap { userId =>
      DBIOAction.sequence(
        extOAuth2Info.params.map { params =>
          params.map { param => OAuth2InfoParam.insertOrUpdate(OAuth2InfoParamRow(userId, param._1, param._2)) }.toSeq
        }.getOrElse(Seq()) :+ OAuth2Info.filter(_.userId === userId).update(OAuth2InfoRow(userId, extOAuth2Info.accessToken,
          extOAuth2Info.tokenType, extOAuth2Info.expiresIn, extOAuth2Info.refreshToken))
      )
    }.transactionally
    db.run(action).map(_ => extOAuth2Info)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param extLoginInfo The login info for which the auth info should be saved.
   * @param extOAuth2Info The auth info to save.
   * @return The saved auth info.
   */
  def save(extLoginInfo: ExtLoginInfo, extOAuth2Info: ExtOAuth2Info): Future[ExtOAuth2Info] = {
    find(extLoginInfo).flatMap {
      case Some(_) => update(extLoginInfo, extOAuth2Info)
      case None => add(extLoginInfo, extOAuth2Info)
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
      loginInfo.providerId === extLoginInfo.providerID && loginInfo.providerKey === extLoginInfo.providerKey
    }.result.head.map(_.userId).flatMap { userId =>
      DBIOAction.sequence(Seq(OAuth2Info.filter(_.userId === userId).delete, OAuth2InfoParam.filter(_.userId === userId).delete))
    }.transactionally
    db.run(action).map(() => _)
  }
}