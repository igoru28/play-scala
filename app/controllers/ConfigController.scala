package controllers

import javax.inject.Singleton

import config.ConfigUtil
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Author igor on 16.05.16.
 */
@Singleton
class ConfigController extends Controller {
  def params(env: String) = Action {
    Ok(Json.toJson(ConfigUtil.configurations(env).map{ case (name, value) =>
      Json.obj("name" -> name, "value" -> value)
    }))
  }

  def environments() = Action {
    Ok(Json.toJson(ConfigUtil.environments))
  }
}
