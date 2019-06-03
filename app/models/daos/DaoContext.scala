package models.daos

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, TotpInfo}
import com.mohiva.play.silhouette.persistence.daos.{AuthInfoDAO, DelegableAuthInfoDAO}
import javax.inject._

/**
  * Handy accessible context containing values for all dao implementations
  *
  * @param userDao
  * @param securityRoleDao
  * @param authTokenDao
  * @param loginInfoDao
  * @param oAuth2InfoDao
  * @param oAuth2InfoDelegableDao
  * @param passwordInfoDao
  * @param passwordInfoDelegableDao
  * @param scratchCodeDao
  * @param totpInfoDao
  * @param totpInfoDelegableDao
  */
@Singleton
class DaoContext @Inject() (
  val userDao: UserDao,
  val securityRoleDao: SecurityRoleDao,
  val authTokenDao: AuthTokenDao,
  val loginInfoDao: LoginInfoDao,
  val oAuth2InfoDao: AuthInfoDAO[OAuth2Info],
  val oAuth2InfoDelegableDao: DelegableAuthInfoDAO[OAuth2Info],
  val passwordInfoDao: AuthInfoDAO[PasswordInfo],
  val passwordInfoDelegableDao: DelegableAuthInfoDAO[PasswordInfo],
  val scratchCodeDao: ScratchCodeDao,
  val totpInfoDao: AuthInfoDAO[TotpInfo],
  val totpInfoDelegableDao: DelegableAuthInfoDAO[TotpInfo],
)
