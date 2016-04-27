package akkatest.hierarchy

import akka.actor.Actor
import io.Logging

/**
 * Author igor on 22.04.16.
 */
case object Report

class StatusCollector extends Actor with Logging {
  private var pongs = Map[String, Set[String]]()

  override def receive: Receive = {
    case Pong(message) =>
      val path = sender().path.toString
      pongs += path -> (pongs.get(path).getOrElse(Set()) + message)
    case Report =>
      sender() ! pongs
      pongs = Map[String, Set[String]]()
  }
}
