package models.daos

import java.time.LocalDate
import models.generated.Tables._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[LoginInfoDaoImpl]]
 */
class LoginInfoDaoSpec extends BaseDaoSpec {
  sequential

  "The LoginInfoDao should" should {
    "correctly find an existing `LoginInfo`" in new Context {
      // create Fixture
      await(userDao.create(testUser1, testLoginInfo1))

      val result = loginInfoDao.find(testLoginInfo1.providerID, testLoginInfo1.providerKey)

      result should not be None
      val loginInfo = result.get.toExt

      loginInfo.providerID should beEqualTo(testLoginInfo1.providerID)
      loginInfo.providerKey should beEqualTo(testLoginInfo1.providerID)
    }

    "correctly create a new `LoginInfo` associated with an user id" in new Context {
      // create Fixture
      val user = await(userDao.createAndFetch(testUser1))

      val result = for {
        _ <- loginInfoDao.create(user.id, testLoginInfo1)
        loginInfo <- loginInfoDao.findById(user.id)
      } yield (loginInfo)

      result should not be None
      val loginInfo = result.get.toExt

      loginInfo.providerID should beEqualTo(testLoginInfo1.providerID)
      loginInfo.providerKey should beEqualTo(testLoginInfo1.providerID)
    }

    "correctly delete a `LoginInfo` by user and provider id" in new Context {
      // create Fixture
      val user = await(userDao.createAndFetch(testUser1))
      await(loginInfoDao.create(user.id, testLoginInfo1))

      val result = await(loginInfoDao.delete(user.id, testLoginInfo1.providerID))
      result should beEqualTo(1)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends BaseContext {
    val testUser1 = UserRow(
      id = 0L,
      firstName = Some("John"),
      lastName = Some("Wick"),
      dateOfBirth = Some(java.sql.Date.valueOf(LocalDate.now())),
      email = Some("test@test.test"),
      avatarUrl = Some("avatar.com"),
      activated = true,
      lastLogin = Some(DateTime.now()),
      modified = Some(DateTime.now())
    )

    val testLoginInfo1 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID", "testProviderKey")

    val userDao: UserDao = daoContext.userDao
    val loginInfoDao: LoginInfoDao = daoContext.loginInfoDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
    await(loginInfoDao.deleteAll)
  }
}
