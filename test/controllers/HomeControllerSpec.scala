package controllers

import models.{Employee, MongoDBService}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}


/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */


class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar {


  val service: MongoDBService = mock[MongoDBService]
  val controller = new HomeController(stubControllerComponents(), service)

  implicit def ec: ExecutionContext = stubControllerComponents().executionContext

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to EMS")
    }

    "render the index page from the application" in {
      val controller = inject[HomeController]
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to EMS")
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to EMS")
    }

    val employeeInfo = Seq("name" -> "John", "age" -> "20", "project" -> "Test Project", "client" -> "Test Client")

    /*"create an employee in mongodb" in {
      import org.mockito.Mockito._
      when(service.insertEmployee(Employee(None, "John", 20, "Test Project", "Test Client", None))).thenReturn(Future(WriteResult))
      val home = controller.createEmployee().apply(FakeRequest(POST, "/employee/new")
        .withFormUrlEncodedBody(employeeInfo: _*))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include("Employee Registration")
    }*/

  }
}
