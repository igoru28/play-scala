package akkatest.session

import akka.actor.Actor

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Author igor on 28.04.16.
 */

case class SessionActor(val name: String) extends Actor {
  private var end: Deadline = 20 seconds fromNow

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = context.system.scheduler.schedule(0 seconds, 5 seconds, self, Check)

  override def receive: Receive = {
    case Tick =>
      end = 20 seconds fromNow
    case Check =>
      if (end.isOverdue()) {
        context.stop(self)
      }
      sender() ! (name, end.timeLeft)
    case Quit => context.stop(self)
  }
}
