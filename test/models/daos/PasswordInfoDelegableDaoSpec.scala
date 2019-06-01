package models.daos

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[PasswordInfoDelegableDao]]
 */
class PasswordInfoDelegableDaoSpec extends BaseDaoSpec {
  sequential

  "The password info delegable dao" should {
    "should save and find PasswordInfo" in new Context {
      val passwordInfoOpt: Option[PasswordInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- delegablePasswordInfoDao.save(testLoginInfo, testPasswordInfo)
        passwordInfo <- delegablePasswordInfoDao.find(testLoginInfo)
      } yield passwordInfo

      passwordInfoOpt should not be None
      val passwordInfo: PasswordInfo = passwordInfoOpt.get
      passwordInfo.hasher should beEqualTo(testPasswordInfo.hasher)
      passwordInfo.password should beEqualTo(testPasswordInfo.password)
      passwordInfo.salt should beEqualTo(testPasswordInfo.salt)
    }

    "should update PasswordInfo" in new Context {
      val passwordInfoOpt: Option[PasswordInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- delegablePasswordInfoDao.save(testLoginInfo, testPasswordInfo)
        _ <- delegablePasswordInfoDao.add(testLoginInfo, testPasswordInfo2)
        passwordInfo <- delegablePasswordInfoDao.find(testLoginInfo)
      } yield passwordInfo

      passwordInfoOpt should not be None
      val passwordInfo: PasswordInfo = passwordInfoOpt.get
      passwordInfo.hasher should beEqualTo(testPasswordInfo2.hasher)
      passwordInfo.password should beEqualTo(testPasswordInfo2.password)
      passwordInfo.salt should beEqualTo(testPasswordInfo2.salt)
    }

    "should remove PasswordInfo" in new Context {
      val passwordInfoOpt: Option[PasswordInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- delegablePasswordInfoDao.save(testLoginInfo, testPasswordInfo)
        passwordInfo <- delegablePasswordInfoDao.find(testLoginInfo)
      } yield passwordInfo

      passwordInfoOpt should not be None

      val emptyOAuth2InfoOpt: Option[PasswordInfo] = for {
        _ <- delegablePasswordInfoDao.remove(testLoginInfo)
        passwordInfo <- delegablePasswordInfoDao.find(testLoginInfo)
      } yield passwordInfo

      passwordInfoOpt should be(None)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends BaseContext {
    val delegablePasswordInfoDao: AuthInfoDAO[PasswordInfo] = daoContext.delegablePasswordInfoDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
