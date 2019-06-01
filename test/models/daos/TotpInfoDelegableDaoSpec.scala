package models.daos

import com.mohiva.play.silhouette.impl.providers.TotpInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[TotpInfoDelegableDao]]
 */
class TotpInfoDelegableDaoSpec extends BaseDaoSpec {
  sequential

  "The totp info delegable dao" should {
    "should save and find TotpInfo" in new Context {
      val totpInfoOpt: Option[TotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testTotpInfo)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None
      val totpInfo: TotpInfo = totpInfoOpt.get
      totpInfo.sharedKey should beEqualTo(testTotpInfo.sharedKey)
      totpInfo.scratchCodes should beEqualTo(testTotpInfo.scratchCodes)
    }

    "should update TotpInfo" in new Context {
      val totpInfoOpt: Option[TotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testTotpInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testTotpInfo2)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None
      val totpInfo: TotpInfo = totpInfoOpt.get
      totpInfo.sharedKey should beEqualTo(testTotpInfo2.sharedKey)
      totpInfo.scratchCodes should beEqualTo(testTotpInfo2.scratchCodes)
    }

    "should remove TotpInfo" in new Context {
      val totpInfoOpt: Option[TotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testTotpInfo)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None

      val emptyOAuth2InfoOpt: Option[TotpInfo] = for {
        _ <- totpInfoDelegableDao.remove(testLoginInfo)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should be(None)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends BaseContext {
    val totpInfoDelegableDao: DelegableAuthInfoDAO[TotpInfo] = daoContext.totpInfoDelegableDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
