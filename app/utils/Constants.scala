package utils

trait Constants {

  val COLLECTION_NAME: String = "emp_details"
  val SEARCHING_FIELDS: List[String] = List("name", "project", "client")
  val SUCCESSFUL_REGISTRATION: (String, String) = "success" -> "Employee has been registered successfully"
  val INVALID_SEARCH_FORM: (String, String) = ("Invalid field to Search", "")
  val REMOVE_WITH_ERROR: String = "Unable to find and delete employee by %s %s"
  val REMOVE_WITH_SUCCESS: String = "Employee(s) has been deleted"
  val UPDATE_WITH_ERROR: String = "Invalid information found to update employee"
  val UPDATE_WITH_SUCCESS: String = "%d Employee(s) with %s %s has been updated"
  val EMPLOYEE_NOT_FOUND: String = "Employee details not found, update operation cancelled"

}
