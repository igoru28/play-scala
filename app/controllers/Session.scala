package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{Props, ActorSystem}
import akkatest.session.SessionManager
import play.api.mvc.Controller

/**
 * Author igor on 28.04.16.
 */
@Singleton
class Session @Inject()(actorSystem: ActorSystem) extends Controller {
  private val sessionManager = actorSystem.actorOf(Props[SessionManager])

  
}
