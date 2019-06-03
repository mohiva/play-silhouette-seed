package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.TotpInfo
import com.mohiva.play.silhouette.persistence.daos.{ AuthInfoDAO, DelegableAuthInfoDAO }
import javax.inject.Inject

import scala.concurrent.Future

/**
 * Adapter implementation that delegates to the actual injected implementation.
 *
 * @param totpInfoDao the injected actual component to delegate to.
 */
class TotpInfoDelegableDao @Inject() (totpInfoDao: AuthInfoDAO[TotpInfo]) extends DelegableAuthInfoDAO[TotpInfo] {
  override def find(loginInfo: LoginInfo): Future[Option[TotpInfo]] = totpInfoDao.find(loginInfo)

  override def add(loginInfo: LoginInfo, totpInfo: TotpInfo): Future[TotpInfo] = totpInfoDao.add(loginInfo, totpInfo)

  override def update(loginInfo: LoginInfo, totpInfo: TotpInfo): Future[TotpInfo] = totpInfoDao.update(loginInfo, totpInfo)

  override def save(loginInfo: LoginInfo, totpInfo: TotpInfo): Future[TotpInfo] = totpInfoDao.save(loginInfo, totpInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = totpInfoDao.remove(loginInfo)
}
