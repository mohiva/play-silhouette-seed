package models.daos

import java.time.LocalDate

import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.TotpInfo
import models.generated.Tables.ScratchCode
import models.generated.Tables._
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import play.api.Application
import play.api.test.{ PlaySpecification, WithApplication }
import utils.AwaitUtil

/**
 * Base trait for all tests that need dao access via Specs2
 */
trait BaseDaoSpec extends PlaySpecification with Mockito {
  /**
   * Returns Dao context instance containing accessible daos.
   * @param app The application instance in context.
   * @return Dao context instance containing accessible daos.
   */
  protected def daoContext(implicit app: Application) = {
    Application.instanceCache[DaoContext].apply(app)
  }

  trait BaseContext extends WithApplication with AwaitUtil {

    val userDao: UserDao = daoContext.userDao

    val testLoginInfo = com.mohiva.play.silhouette.api.LoginInfo("testProviderID", "testProviderKey")

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

    val testOAuth2Info = OAuth2Info(
      accessToken = "testToken",
      tokenType = Some("tokenType"),
      expiresIn = Some(10),
      refreshToken = Some("refreshToken")
    )

    val testOAuth2Info2 = OAuth2Info(
      accessToken = "testToken2",
      tokenType = Some("tokenType2"),
      expiresIn = Some(20),
      refreshToken = Some("refreshToken2")
    )

    val testPasswordInfo = PasswordInfo(
      hasher = "hasher",
      password = "password",
      salt = Some("salt")
    )

    val testPasswordInfo2 = PasswordInfo(
      hasher = "hasher2",
      password = "password2",
      salt = Some("salt2")
    )

    val testTotpInfo = TotpInfo(
      sharedKey = "sharedKey",
      scratchCodes = Seq(testPasswordInfo)
    )

    val testTotpInfo2 = TotpInfo(
      sharedKey = "sharedKey2",
      scratchCodes = Seq(testPasswordInfo2)
    )
  }
}
