package models.daos

import models.generated.Tables._
import org.joda.time._
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[UserDaoImpl]]
 */
class UserDaoSpec extends DaoSpecLike {
  sequential

  "The user dao" should {
    "should update an user correctly" in new Context {
      val userOpt: Option[UserRow] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        u <- userDao.find(testLoginInfo)
        _ <- userDao.update(u.get.copy(firstName = "Harry", lastName = "Potter"))
        user <- userDao.findById(u.get.id)
      } yield user

      userOpt should not be None
      val updated: UserRow = userOpt.get
      updated.firstName should beSome("Harry")
      updated.lastName should beSome("Potter")
      updated.birthDate should beEqualTo(testUser.birthDate)
      updated.email should beEqualTo(testUser.email)
      updated.avatarUrl should beEqualTo(testUser.avatarUrl)
      updated.activated should beTrue
      updated.lastLogin should not be None
      updated.modified should not be None
    }

    "create an user correctly" in new Context {
      val (user, all): (UserRow, Seq[UserRow]) = for {
        user <- userDao.createAndFetch(testUser)
        all <- userDao.findAll()
      } yield (user, all)

      user.firstName should beEqualTo(testUser.firstName)
      user.lastName should beEqualTo(testUser.lastName)
      user.birthDate should beEqualTo(testUser.birthDate)
      user.email should beEqualTo(testUser.email)
      user.avatarUrl should beEqualTo(testUser.avatarUrl)
      user.activated should beTrue
      user.lastLogin should not be None
      user.modified should not be None

      all.size should beEqualTo(1)
    }

    "create an user with LoginInfo correctly" in new Context {
      val (userOpt, nonExistingUserOpt, all): (Option[UserRow], Option[UserRow], Seq[UserRow]) = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- userDao.create(testUser2, testLoginInfo2)
        existingUser <- userDao.find(testLoginInfo)
        nonExistingUser <- userDao.find(testLoginInfo3)
        all <- userDao.findAll()
      } yield (existingUser, nonExistingUser, all)

      nonExistingUserOpt should be(None)

      userOpt should not be None
      val user: UserRow = userOpt.get
      user.firstName should beEqualTo(testUser.firstName)
      user.lastName should beEqualTo(testUser.lastName)
      user.birthDate should beEqualTo(testUser.birthDate)
      user.email should beEqualTo(testUser.email)
      user.avatarUrl should beEqualTo(testUser.avatarUrl)
      user.activated should beTrue
      user.lastLogin should not be None
      user.modified should not be None

      all.size should beEqualTo(2)
    }

    "delete an user correctly" in new Context {
      val (nonExists, exists): (Option[UserRow], Option[UserRow]) = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- userDao.create(testUser2, testLoginInfo2)
        user <- userDao.find(testLoginInfo)
        _ <- userDao.delete(user.get.id)
        nonExists <- userDao.find(testLoginInfo)
        exists <- userDao.find(testLoginInfo2)
      } yield (nonExists, exists)

      nonExists should be(None)

      exists should not be None
      val user: UserRow = exists.get
      user.firstName should beEqualTo(testUser2.firstName)
      user.lastName should beEqualTo(testUser2.lastName)
      user.birthDate should beEqualTo(testUser2.birthDate)
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
  trait Context extends WithApplication with DaoSpecScope {

    val testUser2 = UserRow(
      id = 0L,
      firstName = "John",
      lastName = "McClane",
      birthDate = new LocalDate(),
      gender = "male",
      email = "test2@test.test",
      mobilePhone = Some("(012)-3456-789"),
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
