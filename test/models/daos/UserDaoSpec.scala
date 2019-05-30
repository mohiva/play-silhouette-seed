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
      // create Fixture
      await(userDao.create(testUser1, testLoginInfo1))

      val userOpt = for {
        u <- userDao.find(testLoginInfo1)
        _ <- userDao.update(u.get.copy(firstName = Some("Harry"), lastName = Some("Potter")))
        user <- userDao.findById(u.get.id)
      } yield user

      userOpt should not be None
      val updated = userOpt.get
      updated.firstName should beEqualTo(Some("Harry"))
      updated.lastName should beEqualTo(Some("Potter"))
      updated.dateOfBirth should not be None
      updated.email should beEqualTo(testUser1.email)
      updated.avatarUrl should beEqualTo(testUser1.avatarUrl)
      updated.activated should beTrue
      updated.lastLogin should not be None
      updated.modified should not be None
    }

    "create an user correctly" in new Context {
      val result = for {
        user <- userDao.createAndFetch(testUser1)
        all <- userDao.findAll()
      } yield (user, all)

      val user: UserRow = result._1
      val all: Seq[UserRow] = result._2

      user.firstName should beEqualTo(testUser1.firstName)
      user.lastName should beEqualTo(testUser1.lastName)
      user.dateOfBirth should not be None
      user.email should beEqualTo(testUser1.email)
      user.avatarUrl should beEqualTo(testUser1.avatarUrl)
      user.activated should beTrue
      user.lastLogin should not be None
      user.modified should not be None

      all.size should beEqualTo(1)
    }

    "create an user with LoginInfo correctly" in new Context {
      // create Fixture
      await(userDao.create(testUser1, testLoginInfo1))
      await(userDao.create(testUser2, testLoginInfo2))

      val result = for {
        existingUser <- userDao.find(testLoginInfo1)
        nonExistingUser <- userDao.find(testLoginInfo3)
        all <- userDao.findAll()
      } yield (existingUser, nonExistingUser, all)

      val userOpt: Option[UserRow] = result._1
      val nonExistingUserOpt: Option[UserRow] = result._2
      val all: Seq[UserRow] = result._3

      nonExistingUserOpt should be(None)

      userOpt should not be None
      val user = userOpt.get
      user.firstName should beEqualTo(testUser1.firstName)
      user.lastName should beEqualTo(testUser1.lastName)
      user.dateOfBirth should not be None
      user.email should beEqualTo(testUser1.email)
      user.avatarUrl should beEqualTo(testUser1.avatarUrl)
      user.activated should beTrue
      user.lastLogin should not be None
      user.modified should not be None

      all.size should beEqualTo(2)
    }

    "delete an user correctly" in new Context {
      // create Fixture
      await(userDao.create(testUser1, testLoginInfo1))
      await(userDao.create(testUser2, testLoginInfo2))

      val result = for {
        user <- userDao.find(testLoginInfo1)
        _ <- userDao.delete(user.get.id)
        nonExists <- userDao.find(testLoginInfo1)
        exists <- userDao.find(testLoginInfo2)
      } yield (nonExists, exists)

      val nonExists: Option[UserRow] = result._1
      val exists: Option[UserRow] = result._2

      nonExists should be(None)

      exists should not be None
      val user = exists.get
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

    val testLoginInfo1 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID", "testProviderKey")
    val testLoginInfo2 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID2", "testProviderKey2")
    val testLoginInfo3 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID3", "testProviderKey3")

    val userDao: UserDao = daoContext.userDao
    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
