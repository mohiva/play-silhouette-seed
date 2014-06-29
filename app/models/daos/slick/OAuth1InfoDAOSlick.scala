package models.daos.slick

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth1Info
import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import scala.concurrent.Future
import models.daos.slick.DBTableDefinitions._
import scala.slick.driver.MySQLDriver.simple._

/**
 * The DAO to store the OAuth1 information.
 */
class OAuth1InfoDAOSlick extends DelegableAuthInfoDAO[OAuth1Info] {

  val db = DBTableDefinitions.db
  
  /**
   * Saves the OAuth1 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth1 info to save.
   * @return The saved OAuth1 info or None if the OAuth1 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {
    Future.successful(
      db withSession { implicit session =>
        val infoId = slickLoginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).first.id.get
        slickOAuth1Infos.filter(_.loginInfoId === infoId).firstOption match {
          case Some(info) =>
            slickOAuth1Infos update DBOAuth1Info(info.id, authInfo.token, authInfo.secret, infoId)
          case None =>
            slickOAuth1Infos insert DBOAuth1Info(None, authInfo.token, authInfo.secret, infoId)
        }
        authInfo
      }
    )
  }

  /**
   * Finds the OAuth1 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth1 info or None if no OAuth1 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth1Info]] = {
    Future.successful(
      db withSession { implicit session =>
        slickLoginInfos.filter(info => info.providerID === loginInfo.providerID && info.providerKey === loginInfo.providerKey).firstOption match {
          case Some(info) =>
            val oAuth1Info = slickOAuth1Infos.filter(_.loginInfoId === info.id).first
            Some(OAuth1Info(oAuth1Info.token, oAuth1Info.secret))
          // case None => None
        }
      }
    )
  }
}
