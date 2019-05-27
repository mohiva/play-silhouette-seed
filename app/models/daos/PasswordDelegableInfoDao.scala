package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.persistence.daos.{ AuthInfoDAO, DelegableAuthInfoDAO }
import com.mohiva.play.silhouette.api.util._
import javax.inject.Inject

import scala.concurrent.Future

/**
 * Adapter implementation that delegates to the actual injected implementation.
 *
 * @param passwordInfoDao the injected actual component to delegate to.
 */
class PasswordDelegableInfoDao @Inject()(passwordInfoDao: AuthInfoDAO[PasswordInfo]) extends DelegableAuthInfoDAO[PasswordInfo] {
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = passwordInfoDao.find(loginInfo)

  override def add(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = passwordInfoDao.add(loginInfo, passwordInfo)

  override def update(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = passwordInfoDao.update(loginInfo, passwordInfo)

  override def save(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = passwordInfoDao.save(loginInfo, passwordInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = passwordInfoDao.remove(loginInfo)
}
