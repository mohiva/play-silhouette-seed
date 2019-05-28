package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.{ AuthInfoDAO, DelegableAuthInfoDAO }
import javax.inject.Inject

import scala.concurrent.Future

/**
 * Adapter implementation that delegates to the actual injected implementation.
 *
 * @param oauth2InfoDao the injected actual component to delegate to.
 */
class OAuth2InfoDelegableDao @Inject() (oauth2InfoDao: AuthInfoDAO[OAuth2Info]) extends DelegableAuthInfoDAO[OAuth2Info] {
  override def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = oauth2InfoDao.find(loginInfo)

  override def add(loginInfo: LoginInfo, oauth2Info: OAuth2Info): Future[OAuth2Info] = oauth2InfoDao.add(loginInfo, oauth2Info)

  override def update(loginInfo: LoginInfo, oauth2Info: OAuth2Info): Future[OAuth2Info] = oauth2InfoDao.update(loginInfo, oauth2Info)

  override def save(loginInfo: LoginInfo, oauth2Info: OAuth2Info): Future[OAuth2Info] = oauth2InfoDao.save(loginInfo, oauth2Info)

  override def remove(loginInfo: LoginInfo): Future[Unit] = oauth2InfoDao.remove(loginInfo)
}
