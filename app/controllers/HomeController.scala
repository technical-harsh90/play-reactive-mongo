package controllers

import javax.inject.Inject
import models.PlayForms.{employeeForm, searchEmployeeForm}
import models.{Employee, MongoDBService}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.Constants

import scala.concurrent.{ExecutionContext, Future}

class HomeController @Inject()(components: ControllerComponents, service: MongoDBService)
  extends AbstractController(components) with I18nSupport with Constants {

  implicit def ec: ExecutionContext = components.executionContext

  /**
    * Renders the landing page
    *
    * @return
    */
  def index = Action {
    Ok(views.html.index())
  }

  /**
    * Creates a new employee
    *
    * @return
    */
  def createEmployee: Action[AnyContent] = Action.async { implicit request =>
    employeeForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.register(formWithErrors)))
      },
      employee => {
        service.insertEmployee(employee).map(_ => Redirect(routes.PageController.employeeRegistrationForm())
          .flashing(SUCCESSFUL_REGISTRATION))
      }
    )
  }

  /**
    * Displays a list of all employees
    *
    * @return
    */
  def showEmployees: Action[AnyContent] = Action.async { implicit request =>
    service.fetchAllEmployees map { employeeList =>
      Ok(views.html.showEmployee(employeeList))
    }
  }

  /**
    * Searches an employee by entity and displays its details
    *
    * @return
    */
  def getEmployee: Action[AnyContent] = Action.async { implicit request =>
    searchEmployeeForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.search(formWithErrors)))
      },
      searchCriteria => {
        val (key, value) = searchCriteria
        if (SEARCHING_FIELDS.contains(key)) {
          service.findEmployeeByEntity(key, value).map {
            list => Ok(views.html.showEmployee(list))
          }
        } else Future(BadRequest(views.html.search(searchEmployeeForm.fill(INVALID_SEARCH_FORM))))
      }
    )
  }

  /**
    * Searches an employee by entity and udpates it
    *
    * @param key   Name of the entity
    * @param value Value of the entity
    * @return
    */
  def updateEmployee(key: String, value: String): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(jsValue) =>
        jsValue.validate[Employee].asOpt.fold(Future(BadRequest(UPDATE_WITH_ERROR))) { employee =>
          service.updateEmployee(key, value, employee) map { updatedEmployees =>
            Ok(UPDATE_WITH_SUCCESS.format(updatedEmployees.length, key, value))
          }
        }

      case None => Future(BadRequest(EMPLOYEE_NOT_FOUND))
    }
  }

  /**
    * Searches an employee by entity and removes it
    *
    * @param key   Name of the entity
    * @param value Value of the entity
    * @return
    */
  def removeEmployee(key: String, value: String): Action[AnyContent] = Action.async { implicit request =>
    service.removeEmployee(key, value) flatMap {
      _.fold(Future(BadRequest(REMOVE_WITH_ERROR.format(key, value)))) {
        _ => Future(Ok(REMOVE_WITH_SUCCESS))
      }
    }
  }
}
