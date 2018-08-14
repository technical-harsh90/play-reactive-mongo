package controllers

import javax.inject.Inject
import models.PlayForms._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

class PageController @Inject()(components: ControllerComponents) extends AbstractController(components) with I18nSupport {

  /**
    * Shows the employee registration form
    * @return
    */
  def employeeRegistrationForm() = Action { implicit request =>
    Ok(views.html.register(employeeForm))
  }

  /**
    * Shows the search employee form
    * @return
    */
  def employeeSearchForm() = Action { implicit request =>
    Ok(views.html.search(searchEmployeeForm))
  }

}