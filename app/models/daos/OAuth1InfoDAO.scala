package models.daos

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth1Info
import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import scala.collection.mutable
import scala.concurrent.Future
import OAuth1InfoDAO._

/**
 * The DAO to store the OAuth1 information.
 */
class OAuth1InfoDAO extends DelegableAuthInfoDAO[OAuth1Info] {

  /**
   * Saves the OAuth1 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth1 info to save.
   * @return The saved OAuth1 info or None if the OAuth1 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {
    data += (loginInfo -> authInfo)
    Future.successful(authInfo)
  }

  /**
   * Finds the OAuth1 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth1 info or None if no OAuth1 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth1Info]] = {
    Future.successful(data.get(loginInfo))
  }
}

/**
 * The companion object.
 */
object OAuth1InfoDAO {

  /**
   * The data store for the OAuth1 info.
   */
  var data: mutable.HashMap[LoginInfo, OAuth1Info] = mutable.HashMap()
}
