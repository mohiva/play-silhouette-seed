package models.daos

import java.util.UUID

import models.generated.Tables
import models.generated.Tables._
import org.joda.time.DateTime
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[AuthTokenDaoImpl]]
 */
class AuthTokenDaoSpec extends DaoSpecLike {
  sequential

  "The auth token dao" should {
    "should find token by ID" in new Context {
      // create Fixture
      val userOpt: Option[UserRow] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        userOpt <- userDao.find(testLoginInfo)
      } yield userOpt
      userOpt must not beEmpty

      await(authTokenDao.create(testToken.copy(userId = userOpt.get.id)))

      // now the actual test
      val result: (Option[AuthTokenRow], Seq[AuthTokenRow]) = for {
        token <- authTokenDao.find(testToken.tokenUuId)
        expiredTokens <- authTokenDao.findExpired(DateTime.now())
      } yield (token, expiredTokens)

      val (authTokenOpt, expiredTokens) = result

      expiredTokens must beEmpty
      authTokenOpt should not be None
      val authToken: AuthTokenRow = authTokenOpt.get
      authToken.tokenUuId should beEqualTo(testToken.tokenUuId)
      authToken.tokenId should beEqualTo(testToken.tokenId)
    }

    "should find expired token" in new Context {
      // create Fixture
      val userOpt: Option[UserRow] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        userOpt <- userDao.find(testLoginInfo)
      } yield userOpt
      userOpt must not beEmpty

      await(authTokenDao.create(testToken.copy(userId = userOpt.get.id, expiry = testToken.expiry.minusDays(2))))

      // now the actual test
      val expiredTokens: Seq[AuthTokenRow] = for {
        expiredTokens <- authTokenDao.findExpired(DateTime.now())
      } yield expiredTokens

      expiredTokens.size should beEqualTo(1)
      val authToken: AuthTokenRow = expiredTokens.head
      authToken.tokenUuId should beEqualTo(testToken.tokenUuId)
      authToken.tokenId should beEqualTo(testToken.tokenId)
    }

    "delete a token correctly" in new Context {
      // create Fixture
      val userOpt: Option[UserRow] = for {
        _ <- userDao.create(testUser, testLoginInfo)
        userOpt <- userDao.find(testLoginInfo)
      } yield userOpt
      userOpt must not beEmpty

      // now the actual test
      val authTokenInfoOpt: Option[Tables.AuthTokenRow] = for {
        _ <- authTokenDao.create(testToken.copy(userId = userOpt.get.id))
        _ <- authTokenDao.remove(testToken.tokenUuId)
        authTokenInfo <- authTokenDao.find(testToken.tokenUuId)
      } yield authTokenInfo

      authTokenInfoOpt should be(None)
    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends WithApplication with DaoSpecScope {

    val authTokenDao: AuthTokenDao = daoContext.authTokenDao

    val testToken = AuthTokenRow(
      userId = 0L,
      tokenId = UUID.randomUUID().toString,
      expiry = DateTime.now().plusDays(1)
    )

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
