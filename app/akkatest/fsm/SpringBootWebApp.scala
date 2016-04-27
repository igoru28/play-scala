package akkatest.fsm

import java.io.File

import config.{ApplicationPropertiesTemplate, ConfigUtil}

/**
  * Author igor on 17.04.16.
  */
class SpringBootWebApp extends AppStateActor {
   override val app: ApplicationPropertiesTemplate = ConfigUtil.springBootWebApp


   override protected def deploy(data: Data, sources: Map[String, Iterator[Int]]): Unit = {
     data.jar = sources.headOption.map(_._1)
     super.deploy(data, sources)
   }

   override protected def start(data: Data): Unit = {
     val builder = new ProcessBuilder()
       .directory(new File(getFileDest("/", data.config)))
       .command(
         "java",
         "-jar",
         data.jar.getOrElse {
           throw new IllegalStateException(s"application ${app.name} is not properly deployed")
         },
         s"--config=${app.config.head._1}")
 //    .inheritIO()
     data.process = Option(builder.start())
     if (!data.process.get.isAlive) {
       throw new IllegalStateException(s"application ${app.name} unexpectedly closed with code ${data.process.get.exitValue()}")
     }
   }


   override protected def stop(data: Data): Unit = {
     data.process.get.destroy()
     log.info(s"application stopped with code ${data.process.get.exitValue()}")
   }

  override protected def isRunning(data: Data): Boolean = data.process.fold(false)(_.isAlive)
}
