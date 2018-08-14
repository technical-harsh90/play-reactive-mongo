package controllers

import helper.TestData
import models.{Employee, MongoDBService}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper._
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


class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar with TestData {


  val service: MongoDBService = mock[MongoDBService]
  val controller = new HomeController(stubControllerComponents(), service)

  implicit def ec: ExecutionContext = stubControllerComponents().executionContext

  "HomeController" should {

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

    "not create an employee in mongodb due to bad data" in {
      val mockResult = mock[WriteResult]
      when(service.insertEmployee(MockBody.employeeInfoInCorrect)).thenReturn(Future(mockResult))
      val home = controller.createEmployee().apply(FakeRequest(POST, "/employee/new")
        .withFormUrlEncodedBody(JsonBody.employeeInfoIncorrect: _*).withCSRFToken)
      status(home) mustBe BAD_REQUEST
    }

    "create an employee in mongodb" in {
      val mockResult = mock[WriteResult]
      when(service.insertEmployee(MockBody.employeeInfoCorrect)).thenReturn(Future(mockResult))
      val home = controller.createEmployee().apply(FakeRequest(POST, "/employee/new")
        .withFormUrlEncodedBody(JsonBody.employeeInfoCorrect: _*))
      status(home) mustBe SEE_OTHER
    }

    "show the list of all employees" in {
      when(service.fetchAllEmployees).thenReturn(Future(List(MockBody.employeeInfoCorrect)))
      val home = controller.showEmployees().apply(FakeRequest(GET, "/show/employees"))
      status(home) mustBe OK
    }

    "not search an employee in mongodb due to bad data" in {
      when(service.findEmployeeByEntity("name", null)).thenReturn(Future(List.empty[Employee]))
      val home = controller.getEmployee.apply(FakeRequest(GET, "/employee/get")
        .withFormUrlEncodedBody(JsonBody.searchWithBlankData: _*).withCSRFToken)
      status(home) mustBe BAD_REQUEST
    }

    "not search an employee in mongodb due to invalid entity" in {
      when(service.findEmployeeByEntity("joiningDate", "August 8, 1990")).thenReturn(Future(List.empty[Employee]))
      val home = controller.getEmployee.apply(FakeRequest(GET, "/employee/get")
        .withFormUrlEncodedBody(JsonBody.searchWithInvalidEntity: _*).withCSRFToken)
      status(home) mustBe BAD_REQUEST
    }

    "search an employee in mongodb with valid entity" in {
      when(service.findEmployeeByEntity("project", "Apple Maps"))
        .thenReturn(Future(List(MockBody.employeeInfoCorrect.copy(project = "Apple Maps"))))
      val home = controller.getEmployee.apply(FakeRequest(GET, "/employee/get")
        .withFormUrlEncodedBody(JsonBody.searchWithValidEntity: _*).withCSRFToken)
      status(home) mustBe OK
    }

    "not update an employee in mongodb due when updated json is not provided" in {
      val home = controller.updateEmployee("name", "John").apply(FakeRequest(POST, "/employee/update?key=name?value=John"))
      status(home) mustBe BAD_REQUEST
    }

    "not update an employee in mongodb due when invalid updated json is provided" in {
      val home = controller.updateEmployee("name", "John").apply(FakeRequest(POST, "/employee/update?key=name?value=John")
        .withJsonBody(Json.toJson((JsonBody.employeeInfoCorrect :+ ("project" -> "Apple Maps")).toMap)))
      status(home) mustBe BAD_REQUEST
    }

    "not remove an employee in mongodb due when invalid entity value is provided" in {
      when(service.removeEmployee("name", "Stewart")).thenReturn(Future(None: Option[Employee]))
      val home = controller.removeEmployee("name", "Stewart").apply(FakeRequest(GET, "/employee/remove?key=name?value=Stewart"))
      status(home) mustBe BAD_REQUEST
    }

    "remove an employee in mongodb due when valid entity value is provided" in {
      when(service.removeEmployee("name", "John")).thenReturn(Future(Some(MockBody.employeeInfoCorrect): Option[Employee]))
      val home = controller.removeEmployee("name", "John").apply(FakeRequest(GET, "/employee/remove?key=name?value=John"))
      status(home) mustBe OK
    }

  }
}
