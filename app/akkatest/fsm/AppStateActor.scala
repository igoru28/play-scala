package akkatest.fsm

import akka.actor.{Actor, FSM, LoggingFSM}
import config.{ApplicationPropertiesTemplate, ConfigUtil}
import io.IO

import scala.concurrent.duration.DurationInt

/**
 * Author igor on 14.04.16.
 */
sealed trait State

case object Created extends State

case object Configured extends State

case object Stopped extends State

case object Running extends State

case class Data(config: Map[String, String] = Map("dest" -> "target/"), var process: Option[Process] = None, var jar: Option[String] = None)

sealed trait Command

case class Configure(val config: Map[String, String]) extends Command

// !!! not valid for distributed systems
case class Deploy(val sources: Map[String, Iterator[Int]], config: Option[Map[String, String]] = None) extends Command

object Start extends Command

object Stop extends Command

object QueryState extends Command

abstract class AppStateActor extends Actor with FSM[State, Data] with LoggingFSM[State, Data] {

  private case object CheckAppIsRunning {
    val timerName = getClass.getSimpleName
  };

  def getFileDest(fileName: String, config:Map[String,String]): String = {
    config.getOrElse("dest", "target") + "/" + app.name + "/" + fileName
  }
  val app: ApplicationPropertiesTemplate

  protected def deploy(data: Data, sources: Map[String, Iterator[Int]]): Unit = {
    sources.map { case (fileName, dataStream) =>
        val target = getFileDest(fileName, data.config)
        log.info(s"writing file $target")
        IO.writeIterator[Int](target, dataStream, (b, out) => out.write(b))
    }
    deployConfig(data)
  }

  protected def deployConfig(data: Data) = {
    ConfigUtil.getConfig(data.config, app).map{ case (configFile, stream) =>
      val target = getFileDest(configFile, data.config)
      log.info(s"writing config file $target")
      IO.writeIterator[String](target, stream, (line, out) => out.write((line+"\n").getBytes()))
    }
  }

  protected def start(data: Data): Unit
  protected def stop(data: Data): Unit
  protected def isRunning(data: Data): Boolean


  startWith(Created, null)
  when(Created) {
    case Event(Configure(config), _) => {
      log.info(s"Configuring application ${app.name}")
      goto(Configured) using Data(config)
    }
    case Event(Deploy(sources, Some(config)), _) =>
      deploy(Data(config), sources)
      goto(Stopped)
  }

  when(Configured) {
    case Event(Configure(config), _) =>
      log.info(s"reconfiguring application ${app.name}")
      stay() using Data(config)
    case Event(Deploy(sources, config), prevData) =>
      log.info(s"deploying application ${app.name}")
      val data = config.fold(prevData)(prevData.copy(_))
      deploy(data, sources)
      goto(Stopped) using data
  }

  when(Stopped) {
    case Event(Configure(config), prevData) =>
      log.info(s"reconfiguring deployed application ${app.name}")
      val data = prevData.copy(config)
      deployConfig(data)
      stay using data
    case Event(Start, data) =>
      log.info(s"starting application ${app.name}")
      start(data)
      goto(Running) using data
  }

  when(Running) {
    case Event(Configure(config), prevData) =>
      val data = prevData.copy(config)
      log.warning(s"reconfiguring running application ${app.name}")
      stop(prevData)
      deployConfig(data)
      start(data)
      stay using data
    case Event(Deploy(sources, newConfig), prevData) =>
      log.warning(s"redeploying running application ${app.name}")
      stop(prevData)
      val data = newConfig.fold(prevData)(prevData.copy(_))
      deploy(data, sources)
      start(data)
      stay using data
    case Event(Stop, data) =>
      log.info(s"stopping application ${app.name}")
      stop(data)
      goto(Stopped)
      
  }

  onTransition {
    case Stopped -> Running => setTimer(CheckAppIsRunning.timerName, CheckAppIsRunning, 5 seconds, true)
    case Running -> Stopped => cancelTimer(CheckAppIsRunning.timerName)
  }

  whenUnhandled {
    case Event(QueryState, _) =>
      sender() ! stateName
      stay
    case Event(CheckAppIsRunning, data) =>
      if (isRunning(data)) stay else goto(Stopped)
  }

  initialize()
}
