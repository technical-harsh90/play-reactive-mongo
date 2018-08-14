package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.test.CSRFTokenHelper._

class PageControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val controller = new PageController(stubControllerComponents())

  "PageController" should {
    "show employee registration page with new instance of Controller" in {
      val result = controller.employeeRegistrationForm().apply(FakeRequest(GET, "/register/employee").withCSRFToken)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Employee Registration")
    }

    "show employee registration page from the application" in {
      val result = inject[PageController].employeeRegistrationForm().apply(FakeRequest(GET, "/register/employee").withCSRFToken)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Employee Registration")
    }

    "show employee registration page from the router" in {
      val request = FakeRequest(GET, "/register/employee")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Employee Registration")
    }

    "show employee search page with new instance of Controller" in {
      val result = controller.employeeSearchForm().apply(FakeRequest(GET, "/search/employee").withCSRFToken)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Employee Enquiry")
    }

    "show employee search page from the application" in {
      val result = inject[PageController].employeeSearchForm().apply(FakeRequest(GET, "/search/employee").withCSRFToken)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Employee Enquiry")
    }

    "show employee search page from the router" in {
      val request = FakeRequest(GET, "/search/employee")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Employee Enquiry")
    }

  }
}
