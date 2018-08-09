package models

import play.api.data._
import play.api.data.Forms._

object PlayForms {

  val employeeForm: Form[Employee] = Form(
    mapping(
      "_id" -> optional(text),
      "name" -> nonEmptyText,
      "age" -> number,
      "project" -> nonEmptyText,
      "client" -> nonEmptyText,
      "joiningDate" -> optional(date)
    )(Employee.apply)(Employee.unapply)
  )

  val searchEmployeeForm: Form[(String, String)] = Form(
    tuple(
      "key" -> nonEmptyText,
      "value" -> nonEmptyText
    )
  )
}
