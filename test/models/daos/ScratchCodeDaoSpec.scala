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
        _ <- totpInfoDelegableDao.add(testLoginInfo, testTotpInfo.copy(scratchCodes = Seq(testScratchCode.copy(userId = user.id).toExt)))
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield (user, totpInfo)

      totpInfoOpt should not be None

      val scratchCodes = await(scratchCodeDao.findAll())
      scratchCodes.size should beEqualTo(1)
      scratchCodes.headOption should not be None
      scratchCodes.head.userId should beEqualTo(user.id)
      scratchCodes.head.hasher should beEqualTo(testScratchCode.hasher)
      scratchCodes.head.password should beEqualTo(testScratchCode.password)
      scratchCodes.head.salt should beEqualTo(testScratchCode.salt)
    }

    "should remove code correctly" in new Context {
      val (user, totpInfoOpt): (UserRow, Option[TotpInfo]) = for {
        user <- userDao.createAndFetch(testUser)
        _ <- loginInfoDao.create(user.id, testLoginInfo)
        _ <- totpInfoDelegableDao.add(testLoginInfo, testTotpInfo.copy(scratchCodes = Seq(testScratchCode.copy(userId = user.id).toExt)))
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield (user, totpInfo)

      totpInfoOpt should not be None

      val scratchCodes = await(scratchCodeDao.findAll())
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
