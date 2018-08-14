package models

import java.util.{Date, UUID}

import javax.inject.Inject
import models.Employee._
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.{JSONCollection, _}
import utils.Constants

import scala.concurrent.{ExecutionContext, Future}

class MongoDBService @Inject()(val reactiveMongoApi: ReactiveMongoApi)(executionContext: ExecutionContext)
  extends Constants {

  implicit def ec: ExecutionContext = executionContext

  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](COLLECTION_NAME))

  def insertEmployee(employee: Employee): Future[WriteResult] = {
    collection.flatMap { col =>
      col.insert(employee.copy(
        id = employee.id.orElse(Some(UUID.randomUUID().toString)),
        joiningDate = Some(new Date())))
    }
  }

  def findEmployeeByEntity(key: String, value: String): Future[List[Employee]] = {
    collection.flatMap(_.find[JsObject, Employee](Json.obj(key -> value)).cursor[Employee]()
      .collect[List](-1, Cursor.FailOnError[List[Employee]]()))
  }

  def fetchAllEmployees: Future[List[Employee]] = {
    collection.flatMap(_.find[JsObject, Employee](Json.obj())
      .sort(Json.obj("joiningDate" -> -1)).cursor[Employee]()
      .collect[List](-1, Cursor.FailOnError[List[Employee]]()))
  }

  def updateEmployee(key: String, value: String, employee: Employee): Future[List[Future[UpdateWriteResult]]] = {
    findEmployeeByEntity(key, value).map { employeeList =>
      employeeList map modify(key, value, employee)
    }
  }

  private def modify(key: String, value: String, employee: Employee): PartialFunction[Employee, Future[UpdateWriteResult]] = {
    case empDetails: Employee => {
      collection.flatMap(_.update(Json.obj(key -> value), employee.copy(id = empDetails.id)))
    }
  }

  def removeEmployee(key: String, value: String): Future[Option[Employee]] = {
    collection.map(_.findAndRemove[JsObject](Json.obj(key -> value))).flatMap(_.map {
      _.result[Employee]
    })
  }

}
