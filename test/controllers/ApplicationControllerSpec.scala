package controllers

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo }
import com.mohiva.play.silhouette.test._
import models.generated.Tables._
import models.services.{ UserService, UserServiceImpl }
import net.codingwell.scalaguice.ScalaModule
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.CSRFTokenHelper._
import play.api.test.{ FakeRequest, PlaySpecification, WithApplication }
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * Test case for the [[controllers.ApplicationController]] class.
 */
class ApplicationControllerSpec extends PlaySpecification with Mockito {
  sequential

  "The `index` action" should {
    "redirect to login page if user is unauthorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ApplicationController.index())
          .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid"))
        )

        status(redirectResult) must be equalTo SEE_OTHER

        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.SignInController.view().toString)

        val Some(unauthorizedResult) = route(app, addCSRFToken(FakeRequest(GET, redirectURL)))

        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Silhouette - Sign In")
      }
    }

    "return 200 if user is authorized" in new Context {
      new WithApplication(application) {
        val Some(result) = route(app, addCSRFToken(FakeRequest(routes.ApplicationController.index())
          .withAuthenticator[DefaultEnv](testLoginInfo))
        )

        status(result) must beEqualTo(OK)
      }
    }
  }

  /**
   * The context.
   */
  trait Context extends Scope {
    /**
     * An identity.
     */
    val testUser = UserRow(
      id = 0L,
      firstName = None,
      lastName = None,
      email = None,
      avatarUrl = None,
      activated = true
    )

    val testLoginInfo = LoginInfo("facebook", "user@facebook.com")
    val testLoginInfoRow = LoginInfoRow(0L, "facebook", "user@facebook.com")

    /**
     * A Silhouette fake environment.
     */
    implicit val ec = scala.concurrent.ExecutionContext.global
    implicit val env: Environment[DefaultEnv] = new FakeEnvironment[DefaultEnv](Seq(testLoginInfo -> testUser))

    class FakeUserService extends UserServiceImpl(null, null) {
      override def loginInfo(user: UserRow): Future[Option[LoginInfoRow]] = {
        Future.successful(if (user == testUser) Some(testLoginInfoRow) else None)
      }
    }

    /**
     * A fake Guice module.
     */
    class FakeModule extends AbstractModule with ScalaModule {
      override def configure() = {
        bind[Environment[DefaultEnv]].toInstance(env)
        bind[UserService].toInstance(new FakeUserService())
      }
    }

    /**
     * The application.
     */
    lazy val application = new GuiceApplicationBuilder()
      .overrides(new FakeModule)
      .build()
  }
}
