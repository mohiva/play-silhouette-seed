package models.daos

import java.time.LocalDate

import models.generated.Tables._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[UserDaoImpl]]
 */
class UserDaoSpec extends BaseDaoSpec {
  sequential

  "The user dao" should {
    "should update an user correctly" in new Context {
      val userOpt: Option[UserRow] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        u <- userDao.find(testLoginInfo)
        _ <- userDao.update(u.get.copy(firstName = Some("Harry"), lastName = Some("Potter")))
        user <- userDao.findById(u.get.id)
      } yield user

      userOpt should not be None
      val updated: UserRow = userOpt.get
      updated.firstName should beSome("Harry")
      updated.lastName should beSome("Potter")
      updated.dateOfBirth should not be None
      updated.email should beEqualTo(testUser.email)
      updated.avatarUrl should beEqualTo(testUser.avatarUrl)
      updated.activated should beTrue
      updated.lastLogin should not be None
      updated.modified should not be None
    }

    "create an user correctly" in new Context {
      val result: (UserRow, Seq[UserRow]) = for {
        user <- userDao.createAndFetch(testUser)
        all <- userDao.findAll()
      } yield (user, all)

      val (user, all) = result

      user.firstName should beEqualTo(testUser.firstName)
      user.lastName should beEqualTo(testUser.lastName)
      user.dateOfBirth should not be None
      user.email should beEqualTo(testUser.email)
      user.avatarUrl should beEqualTo(testUser.avatarUrl)
      user.activated should beTrue
      user.lastLogin should not be None
      user.modified should not be None

      all.size should beEqualTo(1)
    }

    "create an user with LoginInfo correctly" in new Context {
      val result: (Option[UserRow], Option[UserRow], Seq[UserRow]) = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- userDao.create(testUser2, testLoginInfo2)
        existingUser <- userDao.find(testLoginInfo)
        nonExistingUser <- userDao.find(testLoginInfo3)
        all <- userDao.findAll()
      } yield (existingUser, nonExistingUser, all)

      val (userOpt, nonExistingUserOpt, all) = result

      nonExistingUserOpt should be(None)

      userOpt should not be None
      val user: UserRow = userOpt.get
      user.firstName should beEqualTo(testUser.firstName)
      user.lastName should beEqualTo(testUser.lastName)
      user.dateOfBirth should not be None
      user.email should beEqualTo(testUser.email)
      user.avatarUrl should beEqualTo(testUser.avatarUrl)
      user.activated should beTrue
      user.lastLogin should not be None
      user.modified should not be None

      all.size should beEqualTo(2)
    }

    "delete an user correctly" in new Context {
      val result: (Option[UserRow], Option[UserRow]) = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- userDao.create(testUser2, testLoginInfo2)
        user <- userDao.find(testLoginInfo)
        _ <- userDao.delete(user.get.id)
        nonExists <- userDao.find(testLoginInfo)
        exists <- userDao.find(testLoginInfo2)
      } yield (nonExists, exists)

      val nonExists: Option[UserRow] = result._1
      val exists: Option[UserRow] = result._2

      nonExists should be(None)

      exists should not be None
      val user: UserRow = exists.get
      user.firstName should beEqualTo(testUser2.firstName)
      user.lastName should beEqualTo(testUser2.lastName)
      user.dateOfBirth should not be None
      user.email should beEqualTo(testUser2.email)
      user.avatarUrl should beEqualTo(testUser2.avatarUrl)
      user.activated should beTrue
      user.lastLogin should not be None
      user.modified should not be None
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends BaseContext {

    val testUser2 = UserRow(
      id = 0L,
      firstName = Some("John"),
      lastName = Some("McClane"),
      dateOfBirth = Some(java.sql.Date.valueOf(LocalDate.now())),
      email = Some("test2@test.test"),
      avatarUrl = Some("avatar2.com"),
      activated = true,
      lastLogin = Some(DateTime.now()),
      modified = Some(DateTime.now())
    )

    val testLoginInfo2 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID2", "testProviderKey2")
    val testLoginInfo3 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID3", "testProviderKey3")

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
