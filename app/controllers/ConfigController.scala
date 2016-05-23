package controllers

import javax.inject.{Inject, Singleton}

import config.ConfigUtil
import model.{Property, PropertyDAO}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

/**
 * Author igor on 16.05.16.
 */
@Singleton
class ConfigController @Inject()(private val propertyDAO: PropertyDAO) extends Controller {
  private def jsonConfig(env: String) = Json.toJson(ConfigUtil.configuration(env, propertyDAO.all).map{ case (name, value) =>
    Json.obj("name" -> name, "value" -> value)
  })

  def params(env: String) = Action {
    Ok(jsonConfig(env))
  }

  def environments = Action {
    Ok(Json.toJson(ConfigUtil.environments))
  }

  def store = Action(parse.json) { request =>
    val env =  (request.body \ "env").as[String]
    val name = (request.body \ "name").as[String]
    val value = (request.body \ "value").as[String]
    propertyDAO.insertOrUpdate(Property(name, value))
    Ok(jsonConfig(env))
  }
}
