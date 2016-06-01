package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{Props, ActorSystem}
import akkatest.fsm.SpringBootWebApp
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Author igor on 16.05.16.
 */
@Singleton
class DeployController @Inject() (val actorSystem: ActorSystem) extends Controller {
  private val applications = Map("spring boot sample" -> actorSystem.actorOf(Props(classOf[SpringBootWebApp])))
  def deploy = Action {
    Ok(views.html.deploy())
  }

  def listApplications = Action {
    Ok(Json.toJson(applications.keySet))
  }
}
