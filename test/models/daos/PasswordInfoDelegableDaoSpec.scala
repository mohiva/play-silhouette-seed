package models.daos

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[PasswordInfoDelegableDao]]
 */
class PasswordInfoDelegableDaoSpec extends DaoSpecLike {
  sequential

  "The password info delegable dao" should {
    "should add and find PasswordInfo" in new Context {
      val passwordInfoOpt: Option[PasswordInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- passwordInfoDelegableDao.add(testLoginInfo, testPasswordInfo)
        passwordInfo <- passwordInfoDelegableDao.find(testLoginInfo)
      } yield passwordInfo

      passwordInfoOpt should not be None

      val passwordInfo: PasswordInfo = passwordInfoOpt.get
      passwordInfo.hasher should beEqualTo(testPasswordInfo.hasher)
      passwordInfo.password should beEqualTo(testPasswordInfo.password)
      passwordInfo.salt should beEqualTo(testPasswordInfo.salt)
    }

    "should save (insert or update) PasswordInfo" in new Context {
      val passwordInfoOpt: Option[PasswordInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- passwordInfoDelegableDao.save(testLoginInfo, testPasswordInfo)
        passwordInfo <- passwordInfoDelegableDao.find(testLoginInfo)
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
        _ <- passwordInfoDelegableDao.add(testLoginInfo, testPasswordInfo)
        _ <- passwordInfoDelegableDao.update(testLoginInfo, testPasswordInfo2)
        passwordInfo <- passwordInfoDelegableDao.find(testLoginInfo)
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
        _ <- passwordInfoDelegableDao.save(testLoginInfo, testPasswordInfo)
        passwordInfo <- passwordInfoDelegableDao.find(testLoginInfo)
      } yield passwordInfo

      passwordInfoOpt should not be None

      val emptyOAuth2InfoOpt: Option[PasswordInfo] = for {
        _ <- passwordInfoDelegableDao.remove(testLoginInfo)
        passwordInfo <- passwordInfoDelegableDao.find(testLoginInfo)
      } yield passwordInfo

      emptyOAuth2InfoOpt should be(None)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends WithApplication with DaoSpecScope {
    val passwordInfoDelegableDao: AuthInfoDAO[PasswordInfo] = daoContext.passwordInfoDelegableDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
