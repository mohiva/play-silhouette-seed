package models.daos

import com.mohiva.play.silhouette.api
import models.generated.Tables
import models.generated.Tables._
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[LoginInfoDaoImpl]]
 */
class LoginInfoDaoSpec extends DaoSpecLike {
  sequential

  "The LoginInfoDao should" should {
    "correctly find an existing `LoginInfo`" in new Context {
      val loginInfoOpt: Option[LoginInfoRow] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        loginInfo <- loginInfoDao.find(testLoginInfo.providerID, testLoginInfo.providerKey)
      } yield loginInfo

      loginInfoOpt should not be None
      val loginInfo: api.LoginInfo = loginInfoOpt.get.toExt

      loginInfo.providerID should beEqualTo(testLoginInfo.providerID)
      loginInfo.providerKey should beEqualTo(testLoginInfo.providerID)
    }

    "correctly create a new `LoginInfo` associated with an user id" in new Context {
      // create Fixture
      val loginInfoOpt: Option[Tables.LoginInfoRow] = for {
        user <- userDao.createAndFetch(testUser)
        _ <- loginInfoDao.create(user.id, testLoginInfo)
        loginInfo <- loginInfoDao.findById(user.id)
      } yield loginInfo

      loginInfoOpt should not be None
      val loginInfo: api.LoginInfo = loginInfoOpt.get.toExt

      loginInfo.providerID should beEqualTo(testLoginInfo.providerID)
      loginInfo.providerKey should beEqualTo(testLoginInfo.providerID)
    }

    "correctly delete a `LoginInfo` by user and provider id" in new Context {
      val loginInfoOpt: Option[LoginInfoRow] = for {
        user <- userDao.createAndFetch(testUser)
        _ <- loginInfoDao.create(user.id, testLoginInfo)
        _ <- loginInfoDao.delete(user.id, testLoginInfo.providerID)
        loginInfo <- loginInfoDao.findById(user.id)
      } yield loginInfo

      loginInfoOpt should be(None)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends WithApplication with DaoSpecScope {

    val loginInfoDao: LoginInfoDao = daoContext.loginInfoDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
