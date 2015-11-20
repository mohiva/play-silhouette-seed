package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.cas.CASAuthInfo
import models.daos.CASAuthInfoDAO._
import play.Logger
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.mutable
import scala.concurrent.Future

/**
 * The DAO to store the CAS information.
 *
 * Note: Not thread safe, demo only.
 *
 * Created by Deter on 2015-11-20.
 */
class CASAuthInfoDAO extends DelegableAuthInfoDAO[CASAuthInfo] {

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[CASAuthInfo]] = {
    val r=data.get(loginInfo)
    Logger.info(s"Find $loginInfo : $r")
    Future.successful(data.get(loginInfo))
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: CASAuthInfo): Future[CASAuthInfo] = {
    Logger.info(s"Add $loginInfo $authInfo")
    data += (loginInfo -> authInfo)
    Future.successful(authInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: CASAuthInfo): Future[CASAuthInfo] = {
    Logger.info(s"Update $loginInfo $authInfo")
    data += (loginInfo -> authInfo)
    Future.successful(authInfo)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, authInfo: CASAuthInfo): Future[CASAuthInfo] = {
    Logger.info(s"Save $loginInfo $authInfo")
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    Logger.info(s"Remove $loginInfo")
    data -= loginInfo
    Future.successful(())
  }
}

/**
 * The companion object.
 */
object CASAuthInfoDAO {

  /**
   * The data store for the OAuth1 info.
   */
  var data: mutable.HashMap[LoginInfo, CASAuthInfo] = mutable.HashMap()
}
