package models.daos

import java.time.LocalDate

import models.generated.Tables._
import org.joda.time.DateTime
import org.scalatest.Matchers
import play.api.test.WithApplication
import utils.AwaitUtil

import scala.concurrent.ExecutionContext.Implicits.global

class UserDaoFunSpec extends AbstractDaoFunSpec with Matchers with AwaitUtil {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  describe("Create user") {
    new WithApplication() {
      val dao = daoContext
      // ensure repeatability of the test
      await(dao.userDao.deleteAll)

      val model = UserRow(
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

      val result = for {
        user <- dao.userDao.createAndFetch(model)
        all <- dao.userDao.findAll()
      } yield (user, all)

      val user: UserRow = result._1
      val all = result._2

      it("user should be correct") {
        user.firstName should equal(model.firstName)
        user.lastName should equal(model.lastName)
        user.dateOfBirth should not be None
        user.email should equal(model.email)
        user.avatarUrl should equal(model.avatarUrl)
        user.activated should not be None
        user.lastLogin should not be None
        user.modified should not be None
      }

      it("there must be only one user") {
        all.size should equal(1)
      }
    }
  }
}
