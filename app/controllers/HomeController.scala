package controllers

import java.util.{Date, UUID}

import javax.inject.Inject
import models.Employee._
import models.Employee
import models.PlayForms._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import play.api.i18n.I18nSupport
import utils.Constants

import scala.concurrent.{ExecutionContext, Future}

class HomeController @Inject()(
                                components: ControllerComponents,
                                val reactiveMongoApi: ReactiveMongoApi,
                                implicit val materializer: akka.stream.Materializer
                              ) extends AbstractController(components)
  with MongoController with ReactiveMongoComponents with I18nSupport with Constants {

  implicit def ec: ExecutionContext = components.executionContext


  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }


  def collection: Future[JSONCollection] = reactiveMongoApi.database.
    map(_.collection[JSONCollection](COLLECTION_NAME))


  def createEmployee: Action[AnyContent] = Action.async { implicit request =>
    employeeForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.register(formWithErrors)))
      },
      employee => {
        collection.flatMap { col =>
          col.insert(employee.copy(
            id = employee.id.orElse(Some(UUID.randomUUID().toString)),
            joiningDate = Some(new Date())))
        }.map(_ => Redirect("/register/employee").flashing(SUCCESSFUL_REGISTRATION))
      }
    )
  }

  def getEmployee: Action[AnyContent] = Action.async { implicit request =>
    searchEmployeeForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.search(formWithErrors)))
      },
      searchCriteria => {
        val (key, value) = searchCriteria
        if (SEARCHING_FIELDS.contains(key)) {
          findEmployees(key, value).map {
            list => Ok(views.html.showEmployee(list))
          }
        } else Future(BadRequest(views.html.search(searchEmployeeForm.fill(INVALID_SEARCH_FORM))))
      }
    )
  }

  def showEmployees: Action[AnyContent] = Action.async { implicit request =>
    fetchAllEmployees map { employeeList =>
      Ok(views.html.showEmployee(employeeList))
    }
  }

  def updateEmployee(key: String, value: String): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(jsValue) =>
        jsValue.validate[Employee].asOpt.fold(Future(BadRequest("Invalid information found to update employee"))) { employee =>
          findEmployees(key, value).map { employeeList =>
            employeeList map modify(key, value, employee)
            Ok(s"${employeeList.length} Employee(s) with $key $value has been updated")
          }
        }

      case None => Future(BadRequest("Employee details not found, update operation cancelled"))
    }
  }

  def modify(key: String, value: String, employee: Employee): PartialFunction[Employee, _] = {
    case empDetails: Employee => collection.flatMap(_.update(Json.obj(key -> value), employee.copy(id = empDetails.id)))
  }

  def removeEmployee(key: String, value: String): Action[AnyContent] = Action.async { implicit request =>
    collection.map(_.findAndRemove[JsObject](Json.obj(key -> value))).map {
      _ flatMap {
        _.result[Employee].fold(Future(BadRequest(s"Unable to find and delete employee by $key $value")))(_ => Future(Ok("Employee(s) has been deleted")))
      }
    }.flatten
  }

  private def findEmployees(key: String, value: String): Future[List[Employee]] = {
    collection.flatMap(_.find[JsObject, Employee](Json.obj(key -> value)).cursor[Employee]()
      .collect[List](-1, Cursor.FailOnError[List[Employee]]()))
  }

  private def fetchAllEmployees: Future[List[Employee]] = {
    collection.flatMap(_.find[JsObject, Employee](Json.obj())
      .sort(Json.obj("joiningDate" -> -1)).cursor[Employee]()
      .collect[List](-1, Cursor.FailOnError[List[Employee]]()))
  }

}