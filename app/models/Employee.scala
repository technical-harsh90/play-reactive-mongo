package models

import java.util.Date

import play.api.libs.json._

object Employee {

  case class Employee(
                       id: Option[String],
                       name: String,
                       age: Int,
                       project: String,
                       client: String,
                       joiningDate: Option[Date])

  implicit object writes extends OWrites[Employee] {
    def writes(employee: Employee): JsObject = Json.obj(
      "_id" -> employee.id,
      "name" -> employee.name,
      "age" -> employee.age,
      "project" -> employee.project,
      "client" -> employee.client,
      "joiningDate" -> employee.joiningDate.fold(new Date)(identity))
  }

  implicit object reads extends Reads[Employee] {
    def reads(json: JsValue): JsResult[Employee] = json match {
      case obj: JsObject => try {
        val id = (obj \ "_id").asOpt[String]
        val name = (obj \ "name").as[String]
        val age = (obj \ "age").as[Int]
        val project = (obj \ "project").as[String]
        val client = (obj \ "client").as[String]
        val joiningDate = (obj \ "joiningDate").asOpt[Date]

        JsSuccess(Employee(id, name, age, project, client, joiningDate))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

}
