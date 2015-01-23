package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.OpenIDInfo
import models.daos.OpenIDInfoDAO._

import scala.collection.mutable
import scala.concurrent.Future

/**
 * The DAO to store the OpenID information.
 *
 * Note: Not thread safe, demo only.
 */
class OpenIDInfoDAO extends DelegableAuthInfoDAO[OpenIDInfo] {

  /**
   * Saves the OpenID info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OpenID info to save.
   * @return The saved OpenID info or None if the OpenID info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OpenIDInfo): Future[OpenIDInfo] = {
    data += (loginInfo -> authInfo)
    Future.successful(authInfo)
  }

  /**
   * Finds the OpenID info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OpenID info or None if no OpenID info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OpenIDInfo]] = {
    Future.successful(data.get(loginInfo))
  }
}

/**
 * The companion object.
 */
object OpenIDInfoDAO {

  /**
   * The data store for the OpenID info.
   */
  var data: mutable.HashMap[LoginInfo, OpenIDInfo] = mutable.HashMap()
}
