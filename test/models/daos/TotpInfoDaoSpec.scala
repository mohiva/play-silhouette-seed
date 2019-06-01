package models.daos

import com.mohiva.play.silhouette.impl.providers.TotpInfo
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[TotpInfoDaoImpl]]
 */
class TotpInfoDaoSpec extends BaseDaoSpec {
  sequential

  "The totp info dao" should {
    "should save and find TotpInfo" in new Context {
      val totpInfoOpt: Option[TotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDao.save(testLoginInfo, testTotpInfo)
        totpInfo <- totpInfoDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None
      val totpInfo: TotpInfo = totpInfoOpt.get
      totpInfo.sharedKey should beEqualTo(testTotpInfo.sharedKey)
      totpInfo.scratchCodes should beEqualTo(testTotpInfo.scratchCodes)
    }

    "should update TotpInfo" in new Context {
      val totpInfoOpt: Option[TotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDao.save(testLoginInfo, testTotpInfo)
        _ <- totpInfoDao.save(testLoginInfo, testTotpInfo2)
        totpInfo <- totpInfoDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None
      val totpInfo: TotpInfo = totpInfoOpt.get
      totpInfo.sharedKey should beEqualTo(testTotpInfo2.sharedKey)
      totpInfo.scratchCodes should beEqualTo(testTotpInfo2.scratchCodes)
    }

    "should remove TotpInfo" in new Context {
      val totpInfoOpt: Option[TotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDao.save(testLoginInfo, testTotpInfo)
        totpInfo <- totpInfoDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None

      val emptyOAuth2InfoOpt: Option[TotpInfo] = for {
        _ <- totpInfoDao.remove(testLoginInfo)
        totpInfo <- totpInfoDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should be(None)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends BaseContext {
    val totpInfoDao: AuthInfoDAO[TotpInfo] = daoContext.totpInfoDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
