package controllers

import javax.inject.Singleton

import play.api.mvc.{Action, Controller}

/**
 * Author igor on 16.05.16.
 */
@Singleton
class DeployController extends Controller {
  private val applications = Map
  def deploy = Action {
    Ok(views.html.deploy())
  }

  def listApplications = Action {

  }
}
