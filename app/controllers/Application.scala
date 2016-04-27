package controllers

import scala.concurrent.duration._
import javax.inject.{Inject, Singleton}

import akka.actor.{Props, ActorSystem}
import akka.pattern._
import akka.util.Timeout
import akkatest.hierarchy._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class Application @Inject()(val actorSystem: ActorSystem) extends Controller {
  val rootActor = actorSystem.actorOf(Props[NodeActor], "root")
  val statusCollector = actorSystem.actorOf(Props[StatusCollector], "collector")
  implicit val timeout = Timeout(10 seconds)
  implicit val nodeWrites = new Writes[Node] {
    override def writes(node: Node): JsValue = {
      Json.obj("path" -> node.path,
        "children" -> node.children.map(writes(_)))
    }
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def actors = Action {
    Ok(Json.toJson(Await.result(rootActor ? GetChildren, Duration.Inf).asInstanceOf[Node]))
  }

  def addChild = Action(parse.json) { request =>
    val path = (request.body \ "path").getOrElse(JsString("user/root")).as[String]
    actorSystem.actorSelection(path) ! AddChild
    Ok(Json.toJson(getChildren))
  }


  def quit = Action(parse.json) { request =>
    val path = (request.body \ "path").getOrElse(JsString("user/root")).as[String]
    val target = actorSystem.actorSelection(path).pathString
    if (!rootActor.path.toString.endsWith(target)) {
      actorSystem.actorSelection(path) ! Quit
    }
    Ok(Json.obj("removing" -> path))
  }

  def messages = Action {
    Ok(Json.toJson(getCollectedStatuses.map{ case (actorPath, message) =>
      Json.obj("path" -> actorPath, "message" -> message.mkString("\n"))
    }))
  }

  def ping = Action(parse.json) { request =>
    val message = (request.body \ "message").getOrElse(JsString("Cannot parse the message")).as[String]
    val path = (request.body \ "path").getOrElse(JsString("user/root")).as[String]
    Ok(Json.obj("pong" -> Await.result(actorSystem.actorSelection(path) ? Ping(message), 10 seconds).asInstanceOf[Pong].message))
  }

  def throwException = Action(parse.json) { request =>
    val message = (request.body \ "message").getOrElse(JsString("Cannot parse the message")).as[String]
    val path = (request.body \ "path").getOrElse(JsString("user/root")).as[String]
    actorSystem.actorSelection(path) ? ThrowException(message)
    Ok(Json.obj("thrown" -> "exception"))
  }

  private def getChildren: Node = Await.result(rootActor ? GetChildren, 30 seconds).asInstanceOf[Node]
  private def getCollectedStatuses: Map[String, Set[String]] = Await.result(statusCollector ? Report, 30 seconds).asInstanceOf[Map[String,Set[String]]]
}
