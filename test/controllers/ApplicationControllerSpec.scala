package controllers

import java.util.UUID

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo }
import com.mohiva.play.silhouette.test._
import models.User
import net.codingwell.scalaguice.ScalaModule
import org.specs2.execute.{ ErrorException, FailureException }
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.CSRFTokenHelper._
import play.api.test.{ FakeRequest, PlaySpecification, WithApplication }
import utils.auth.DefaultEnv

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test case for the [[controllers.ApplicationController]] class.
 */
class ApplicationControllerSpec extends PlaySpecification with Mockito {
  sequential

  "The `index` action" should {
    "redirect to login page if user is unauthorized" in new Context {
      new WithApplication(application) {
        val redirectResult = route(app, FakeRequest(routes.ApplicationController.index())
          .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid"))
        ).getOrElse(throw FailureException(failure("required Some")))

        status(redirectResult) must be equalTo SEE_OTHER

        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.SignInController.view().toString)

        val unauthorizedResult = route(app, addCSRFToken(FakeRequest(GET, redirectURL)))
          .getOrElse(throw FailureException(failure("required Some")))

        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Silhouette - Sign In")
      }
    }

    "return 200 if user is authorized" in new Context {
      new WithApplication(application) {
        val result = route(app, addCSRFToken(FakeRequest(routes.ApplicationController.index())
          .withAuthenticator[DefaultEnv](identity.loginInfo))
        ).getOrElse(throw FailureException(failure("required Some")))

        status(result) must beEqualTo(OK)
      }
    }
  }

  /**
   * The context.
   */
  trait Context extends Scope {

    /**
     * A fake Guice module.
     */
    class FakeModule extends AbstractModule with ScalaModule {
      override def configure() = {
        bind[Environment[DefaultEnv]].toInstance(env)
      }
    }

    /**
     * An identity.
     */
    val identity = User(
      userID = UUID.randomUUID(),
      loginInfo = LoginInfo("facebook", "user@facebook.com"),
      firstName = None,
      lastName = None,
      fullName = None,
      email = None,
      avatarURL = None,
      activated = true
    )

    /**
     * A Silhouette fake environment.
     */
    implicit val env: Environment[DefaultEnv] = new FakeEnvironment[DefaultEnv](Seq(identity.loginInfo -> identity))

    /**
     * The application.
     */
    lazy val application = new GuiceApplicationBuilder()
      .overrides(new FakeModule)
      .build()
  }
}
