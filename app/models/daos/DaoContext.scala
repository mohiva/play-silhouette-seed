package models.daos

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, TotpInfo}
import com.mohiva.play.silhouette.persistence.daos.{AuthInfoDAO, DelegableAuthInfoDAO}
import javax.inject._

@Singleton
class DaoContext @Inject() (
  val userDao: UserDao,
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
