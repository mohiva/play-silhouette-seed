package models.daos

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth2Info
import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import scala.collection.mutable
import scala.concurrent.Future
import OAuth2InfoDAO._

/**
 * The DAO to store the OAuth2 information.
 */
class OAuth2InfoDAO extends DelegableAuthInfoDAO[OAuth2Info] {

  /**
   * Saves the OAuth2 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth2 info to save.
   * @return The saved OAuth2 info or None if the OAuth2 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    data += (loginInfo -> authInfo)
    Future.successful(authInfo)
  }

  /**
   * Finds the OAuth2 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth2 info or None if no OAuth2 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    Future.successful(data.get(loginInfo))
  }
}

/**
 * The companion object.
 */
object OAuth2InfoDAO {

  /**
   * The data store for the OAuth2 info.
   */
  var data: mutable.HashMap[LoginInfo, OAuth2Info] = mutable.HashMap()
}
