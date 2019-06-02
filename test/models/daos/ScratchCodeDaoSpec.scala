package models.daos

import com.mohiva.play.silhouette.impl.providers.TotpInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
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
      val (user, totpInfoOpt): (UserRow, Option[TotpInfo]) = for {
        user <- userDao.createAndFetch(testUser)
        _ <- loginInfoDao.create(user.id, testLoginInfo)
        _ <- totpInfoDelegableDao.add(testLoginInfo, testTotpInfo.copy(scratchCodes = Seq()))
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield (user, totpInfo)

      totpInfoOpt should not be None

      val scratchCodes: Seq[ScratchCodeRow] = for {
        _ <- scratchCodeDao.create(testScratchCode.copy(userId = user.id))
        scratchCodes <- scratchCodeDao.findAll()
      } yield scratchCodes

      scratchCodes.size should beEqualTo(1)
    }

    "should remove code correctly" in new Context {
      val (user, totpInfoOpt): (UserRow, Option[TotpInfo]) = for {
        user <- userDao.createAndFetch(testUser)
        _ <- loginInfoDao.create(user.id, testLoginInfo)
        _ <- totpInfoDelegableDao.add(testLoginInfo, testTotpInfo.copy(scratchCodes = Seq()))
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield (user, totpInfo)

      totpInfoOpt should not be None

      val scratchCodes: Seq[ScratchCodeRow] = for {
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

    val loginInfoDao: LoginInfoDao = daoContext.loginInfoDao
    val totpInfoDelegableDao: DelegableAuthInfoDAO[TotpInfo] = daoContext.totpInfoDelegableDao
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
