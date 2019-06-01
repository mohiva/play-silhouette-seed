package models.daos

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.{ AuthInfoDAO, DelegableAuthInfoDAO }
import javax.inject._

@Singleton
class DaoContext @Inject() (
  val userDao: UserDao,
  val authTokenDao: AuthTokenDao,
  val loginInfoDao: LoginInfoDao,
  val oAuth2InfoDao: AuthInfoDAO[OAuth2Info],
  val delegableAuthInfoDao: DelegableAuthInfoDAO[OAuth2Info],
  val passwordInfoDao: AuthInfoDAO[PasswordInfo],
  val delegablePasswordInfoDao: DelegableAuthInfoDAO[PasswordInfo]
)
