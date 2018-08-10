package utils

trait Constants {

  val COLLECTION_NAME: String = "emp_details"
  val SEARCHING_FIELDS: List[String] = List("name", "project", "client")
  val SUCCESSFUL_REGISTRATION: (String, String) = "success" -> "Employee has been registered successfully"
  val INVALID_SEARCH_FORM: (String, String) = ("Invalid field to Search", "")
}
