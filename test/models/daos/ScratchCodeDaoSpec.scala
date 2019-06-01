package models.daos

import models.generated.Tables._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[ScratchCodeDao]]
 */
class ScratchCodeDaoSpec extends BaseDaoSpec {
  sequential

  "The scratch code dao" should {
    "should create code correctly" in new Context {
      val scratchCodes: Seq[ScratchCodeRow] = for {
        user <- userDao.createAndFetch(testUser)
        _ <- scratchCodeDao.create(testScratchCode.copy(userId = user.id))
        scratchCodes <- scratchCodeDao.findAll()
      } yield scratchCodes

      scratchCodes.size should beEqualTo(1)
    }

    "should remove code correctly" in new Context {
      val scratchCodes: Seq[ScratchCodeRow] = for {
        user <- userDao.createAndFetch(testUser)
        _ <- scratchCodeDao.create(testScratchCode.copy(userId = user.id))
        scratchCodes <- scratchCodeDao.findAll()
      } yield scratchCodes

      scratchCodes.size should beEqualTo(1)

      val newScratchCodes: Seq[ScratchCodeRow] = for {
        _ <- scratchCodeDao.delete(scratchCodes.head.userId)
        scratchCodes <- scratchCodeDao.findAll()
      } yield scratchCodes

      newScratchCodes.size should beEqualTo(0)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends BaseContext {

    val scratchCodeDao: ScratchCodeDao = daoContext.scratchCodeDao

    val testScratchCode = ScratchCodeRow(
      userId = 1,
      hasher = "hasher",
      password = "password",
      salt = Some("salt"),
      modified = Some(DateTime.now())
    )

    // ensure repeatability of the test
    await(userDao.deleteAll)
    await(scratchCodeDao.deleteAll)
  }

}
