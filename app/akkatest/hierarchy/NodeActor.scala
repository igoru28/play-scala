package akkatest.hierarchy

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{SupervisorStrategy, Actor, Props}
import akka.pattern
import akka.util.Timeout
import io.Logging

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

/**
 * Author igor on 17.04.16.
 */
case class Node(path: String, children: Seq[Node])
case object GetChildren
case object AddChild
case object Quit
case class Ping(message: String)
case class Pong(message: String)
case class ThrowException(message: String)

class NodeActor extends Actor with Logging {
  implicit val timeout = Timeout(10 seconds)
  private val collectorSelection = context.actorSelection(context.system / "collector")


  override def supervisorStrategy: SupervisorStrategy =
    akka.actor.OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 20 seconds, loggingEnabled = true) {
      case e: Exception =>
        self ! Ping(s"exception in child ${e.toString}")
        Restart
    }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = context.parent ! Pong(s"created actor ${self.path}")


  @throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    context.parent ! Ping(s"restarted actor ${self.path}, caused by $reason")
  }


  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    context.parent ! Ping(s"stopped child ${self.path.toString}")
  }

  def hierarchy(path: String) = {
    Node(self.path.toString,
    context.children.map{child =>
      Await.result(pattern.ask(child, GetChildren), Duration.Inf).asInstanceOf[Node]
    }.toSeq)
  }
  override def receive: Receive = {
    case GetChildren => sender() ! hierarchy(self.path.toString)
    case AddChild =>
      val childRef = context.actorOf(Props[NodeActor])
      childRef ! Ping("created")
      self ! Ping(s"created child: ${childRef.path}")
    case Ping(message) =>
      sender() ! Pong(message)
      collectorSelection ! Pong(message)
//    case pong: Pong => collectorSelection ! pong
    case Quit =>
      context.parent ! Ping(s"stopping child ${self.path.toString}")
      context.stop(self)
    case ThrowException(message) =>
      throw new RuntimeException(message)
  }
}
