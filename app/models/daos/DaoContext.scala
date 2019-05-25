package models.daos

import javax.inject._

@Singleton
class DaoContext @Inject() (
  val userDao: UserDao,
  val authTokenDao: AuthTokenDao,
  val loginInfoDao: LoginInfoDao)
