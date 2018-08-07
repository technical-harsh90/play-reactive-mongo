package controllers

import java.util.{Date, UUID}
import javax.inject.Inject
import models.Employee._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import scala.concurrent.Future

class HomeController @Inject()(
                                components: ControllerComponents,
                                val reactiveMongoApi: ReactiveMongoApi,
                                implicit val materializer: akka.stream.Materializer
                              ) extends AbstractController(components)
  with MongoController with ReactiveMongoComponents {

  implicit def ec = components.executionContext

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }


  def collection: Future[JSONCollection] = reactiveMongoApi.database.
    map(_.collection[JSONCollection]("emp_details"))


  def createEmployee: Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(jsValue) =>
        jsValue.validate[Employee].asOpt.fold(Future(BadRequest("Invalid information found for employee registration"))) { employee =>
          collection.flatMap(_.insert(employee.copy(
            id = employee.id.orElse(Some(UUID.randomUUID().toString)),
            joiningDate = Some(new Date()))
          )).map(_ => Ok(s"Employee ${employee.name} has been registered"))
        }

      case None => Future(BadRequest("Employee details not found"))
    }
  }

  def getEmployee(key: String, value: String): Action[AnyContent] = Action.async { implicit request =>
    findEmployees(key, value).map {
      list => Ok(Json.toJson(list))
    }
  }

  private def findEmployees(key: String, value: String): Future[List[Employee]] = {
    collection.flatMap(_.find[JsObject, Employee](Json.obj(key -> value)).cursor[Employee]()
      .collect[List](25, Cursor.FailOnError[List[Employee]]()))
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
}