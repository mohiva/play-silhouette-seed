package models.daos

import java.time.LocalDate

import models.generated.Tables._
import org.joda.time.DateTime
import org.scalatest.Matchers
import play.api.test.WithApplication
import utils.AwaitUtil

import scala.concurrent.ExecutionContext.Implicits.global

class UserDaoFunSpec extends AbstractDaoFunSpec with Matchers with AwaitUtil {

  val testUser = UserRow(
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

  val testLoginInfo = com.mohiva.play.silhouette.api.LoginInfo("testProviderID", "testProviderKey")
  val testLoginInfo2 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID2", "testProviderKey2")
  val testLoginInfo3 = com.mohiva.play.silhouette.api.LoginInfo("testProviderID3", "testProviderKey3")

  describe("Update user") {
    new WithApplication() {
      val dao: UserDao = daoContext.userDao
      // ensure repeatability of the test
      await(dao.deleteAll)
      await(dao.create(testUser, testLoginInfo))

      val userOpt = for {
        u <- dao.find(testLoginInfo)
        _ <- dao.update(u.get.copy(firstName = Some("Harry"), lastName = Some("Potter")))
        user <- dao.findById(u.get.id)
      } yield user

      it("updated user should be correct") {
        userOpt should not be None
        val updated = userOpt.get
        updated.firstName should equal(Some("Harry"))
        updated.lastName should equal(Some("Potter"))
        updated.dateOfBirth should not be None
        updated.email should equal(testUser.email)
        updated.avatarUrl should equal(testUser.avatarUrl)
        updated.activated should not be None
        updated.lastLogin should not be None
        updated.modified should not be None
      }
    }
  }

  describe("Create user") {
    new WithApplication() {
      val dao: UserDao = daoContext.userDao
      // ensure repeatability of the test
      await(dao.deleteAll)

      val result = for {
        user <- dao.createAndFetch(testUser)
        all <- dao.findAll()
      } yield (user, all)

      val user: UserRow = result._1
      val all: Seq[UserRow] = result._2

      it("user should be correct") {
        user.firstName should equal(testUser.firstName)
        user.lastName should equal(testUser.lastName)
        user.dateOfBirth should not be None
        user.email should equal(testUser.email)
        user.avatarUrl should equal(testUser.avatarUrl)
        user.activated should not be None
        user.lastLogin should not be None
        user.modified should not be None
      }

      it("there must be only one user") {
        all.size should equal(1)
      }
    }
  }

  describe("Create user with LoginInfo") {
    new WithApplication() {
      val dao: UserDao = daoContext.userDao
      // ensure repeatability of the test
      await(dao.deleteAll)

      await(dao.create(testUser, testLoginInfo))
      await(dao.create(testUser2, testLoginInfo2))

      val result = for {
        existingUser <- dao.find(testLoginInfo)
        nonExistingUser <- dao.find(testLoginInfo3)
        all <- dao.findAll()
      } yield (existingUser, nonExistingUser, all)

      val userOpt: Option[UserRow] = result._1
      val nonExistingUserOpt: Option[UserRow] = result._2
      val all: Seq[UserRow] = result._3

      it("non existing user should be None") {
        nonExistingUserOpt should be(None)
      }

      it("user should be correct") {
        userOpt should not be None
        val user = userOpt.get
        user.firstName should equal(testUser.firstName)
        user.lastName should equal(testUser.lastName)
        user.dateOfBirth should not be None
        user.email should equal(testUser.email)
        user.avatarUrl should equal(testUser.avatarUrl)
        user.activated should not be None
        user.lastLogin should not be None
        user.modified should not be None
      }

      it("there must be two users") {
        all.size should equal(2)
      }
    }
  }

  describe("Delete user") {
    new WithApplication() {
      val dao: UserDao = daoContext.userDao

      val result = for {
        user <- dao.find(testLoginInfo)
        _ <- dao.delete(user.get.id)
        nonExists <- dao.find(testLoginInfo)
        exists <- dao.find(testLoginInfo2)
      } yield (nonExists, exists)

      val nonExists: Option[UserRow] = result._1
      val exists: Option[UserRow] = result._2

      it("Wick should be None") {
        nonExists should be(None)
      }

      it("McClane should be correct") {
        exists should not be None
        val user = exists.get
        user.firstName should equal(testUser2.firstName)
        user.lastName should equal(testUser2.lastName)
        user.dateOfBirth should not be None
        user.email should equal(testUser2.email)
        user.avatarUrl should equal(testUser2.avatarUrl)
        user.activated should not be None
        user.lastLogin should not be None
        user.modified should not be None
      }
    }
  }
}
