package models.daos

import com.mohiva.play.silhouette.impl.providers.GoogleTotpInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[GoogleTotpInfoDelegableDao]]
 */
class GoogleTotpInfoDelegableDaoSpec extends DaoSpecLike {
  sequential

  "The totp info delegable dao" should {
    "should add and find GoogleTotpInfo" in new Context {
      val totpInfoOpt: Option[GoogleTotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDelegableDao.add(testLoginInfo, testGoogleTotpInfo)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None

      val totpInfo: GoogleTotpInfo = totpInfoOpt.get
      totpInfo.sharedKey should beEqualTo(testGoogleTotpInfo.sharedKey)
      totpInfo.scratchCodes should beEqualTo(testGoogleTotpInfo.scratchCodes)
    }

    "should save (insert or update) and find GoogleTotpInfo" in new Context {
      val totpInfoOpt: Option[GoogleTotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testGoogleTotpInfo)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None

      val totpInfo: GoogleTotpInfo = totpInfoOpt.get
      totpInfo.sharedKey should beEqualTo(testGoogleTotpInfo.sharedKey)
      totpInfo.scratchCodes should beEqualTo(testGoogleTotpInfo.scratchCodes)
    }

    /*
    // TODO: re-enable once the #update method is properly implemented
    "should update GoogleTotpInfo" in new Context {
      val totpInfoOpt: Option[GoogleTotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testGoogleTotpInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testGoogleTotpInfo2)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None
      val totpInfo: GoogleTotpInfo = totpInfoOpt.get
      totpInfo.sharedKey should beEqualTo(testGoogleTotpInfo2.sharedKey)
      totpInfo.scratchCodes should beEqualTo(testGoogleTotpInfo2.scratchCodes)
    }
*/

    "should remove GoogleTotpInfo" in new Context {
      val totpInfoOpt: Option[GoogleTotpInfo] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        _ <- totpInfoDelegableDao.save(testLoginInfo, testGoogleTotpInfo)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      totpInfoOpt should not be None

      val emptyOAuth2InfoOpt: Option[GoogleTotpInfo] = for {
        _ <- totpInfoDelegableDao.remove(testLoginInfo)
        totpInfo <- totpInfoDelegableDao.find(testLoginInfo)
      } yield totpInfo

      emptyOAuth2InfoOpt should be(None)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends WithApplication with DaoSpecScope {
    val totpInfoDelegableDao: DelegableAuthInfoDAO[GoogleTotpInfo] = daoContext.totpInfoDelegableDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
