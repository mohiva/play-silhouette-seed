package models.daos.slick

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth2Info
import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import scala.concurrent.Future
import models.daos.slick.DBTableDefinitions._
import scala.slick.driver.MySQLDriver.simple._

/**
 * The DAO to store the OAuth2 information.
 */
class OAuth2InfoDAOSlick extends DelegableAuthInfoDAO[OAuth2Info] {

  val db = DBTableDefinitions.db
  
  /**
   * Saves the OAuth2 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth2 info to save.
   * @return The saved OAuth2 info or None if the OAuth2 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    Future.successful(
      db withSession { implicit session =>
        val infoId = slickLoginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).first.id.get
        slickOAuth2Infos.filter(_.loginInfoId === infoId).firstOption match {
          case Some(info) =>
            slickOAuth2Infos update DBOAuth2Info(info.id, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId)
          case None => slickOAuth2Infos insert DBOAuth2Info(None, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId) 
        }
        authInfo
      }
    )
  }

  /**
   * Finds the OAuth2 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth2 info or None if no OAuth2 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    Future.successful(
      db withSession { implicit session =>
        slickLoginInfos.filter(info => info.providerID === loginInfo.providerID && info.providerKey === loginInfo.providerKey).firstOption match {
          case Some(info) =>
            val oAuth2Info = slickOAuth2Infos.filter(_.loginInfoId === info.id).first
            Some(OAuth2Info(oAuth2Info.accessToken, oAuth2Info.tokenType, oAuth2Info.expiresIn, oAuth2Info.refreshToken))
          case None => None
        }
      }
    )
  }
}
