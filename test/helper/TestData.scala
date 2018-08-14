package helper

import models.Employee

trait TestData {

  object JsonBody {
    val employeeInfoCorrect = Seq("name" -> "John", "age" -> "20", "project" -> "Test Project", "client" -> "Test Client")
    val employeeInfoIncorrect = Seq("age" -> "20", "project" -> "Test Project", "client" -> "Test Client")
    val searchWithBlankData = Seq("key" -> "name", "value" -> null)
    val searchWithInvalidEntity = Seq("key" -> "joiningDate", "value" -> "August 8, 1990")
    val searchWithValidEntity = Seq("key" -> "project", "value" -> "Apple Maps")
  }

  object MockBody {
    val employeeInfoCorrect = Employee(None, "John", 20, "Test Project", "Test Client", None)
    val employeeInfoInCorrect = Employee(None, null, 20, "Test Project", "Test Client", None)
  }

}