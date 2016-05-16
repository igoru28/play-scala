package akkatest.session

import akka.actor.{Actor, ActorRef, Props}
import akka.routing._

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Author igor on 28.04.16.
 */

trait AddressedMessage {
  val name: String
}

case class ActorByNameRoutee(actorRef: ActorRef, name: String) extends Routee {

  override def send(message: Any, sender: ActorRef): Unit = {
    sender ! Await.result(akka.pattern.ask(actorRef).?(message)(10 seconds),10 seconds)
  }
}

case class RoutingByNameLogic() extends RoutingLogic {

  override def select(message: Any, routees: IndexedSeq[Routee]): Routee = {
    if (message.isInstanceOf[AddressedMessage]) {
      routees.find(routee =>
        routee.isInstanceOf[ActorByNameRoutee]
          && routee.asInstanceOf[ActorByNameRoutee].name == message.asInstanceOf[AddressedMessage].name)
        .getOrElse(NoRoutee)
    } else {
      NoRoutee
    }
  }
}


case class StartSession(override val name: String) extends AddressedMessage
case class Check(override val name: String) extends AddressedMessage
case class Tick(override val name: String) extends AddressedMessage
case class Quit(override val name: String) extends AddressedMessage
case object CheckAll

class SessionManager extends Actor{
  private val nameRouter = Router(new RoutingByNameLogic, Vector())


  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    context.system.scheduler.schedule(0 seconds, 5 seconds, self, Check("*"))
  }

  override def receive: Receive = {
    case StartSession(name) =>
      nameRouter.addRoutee(ActorByNameRoutee(context.actorOf(Props(classOf[SessionActor], name)), name))
    case msg: AddressedMessage =>
      nameRouter.route(msg, sender())
  }
}
