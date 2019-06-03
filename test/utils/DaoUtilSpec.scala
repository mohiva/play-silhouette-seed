package utils

import org.specs2.mock.Mockito
import org.specs2.mutable._
import play.api.test.WithApplication

import scala.concurrent._

/**
 * Test suite for the [[DaoUtil]] implementation
 */
class DaoUtilSpec extends Specification with Mockito {

  "DaoUtil's simplify method" should {
    "produce the correct result for multiple instances of A" in new Context {
      val fixture: Future[Seq[(String, Option[String])]] = Future.successful(Seq("a1" -> Some("b1"), "a1" -> Some("b2"), "a1" -> Some("b3"), "a2" -> Some("b4")))
      implicit val ec = ExecutionContext.global
      val result = simplify[String, String](fixture)
      val oracle: Future[Option[(String, Seq[String])]] = Future.successful(Some("a1" -> Seq("b1", "b2", "b3")))
      result.toString should beEqualTo(oracle.toString)
    }

    "handle empty results" in new Context {
      val fixture: Future[Seq[(String, Option[String])]] = Future.successful(Seq())
      implicit val ec = ExecutionContext.global
      val result = simplify[String, String](fixture)
      result.toString should beEqualTo(Future.successful(None).toString)
    }
  }

  trait Context extends WithApplication with DaoUtil
}
