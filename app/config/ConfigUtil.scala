package config

import java.io.{BufferedReader, InputStreamReader}
import java.util.Properties

import io.Finalizer

import scala.collection.JavaConverters._
import scala.util.matching.Regex


/**
 * Author igor on 29.01.16.
 */
case class ApplicationPropertiesTemplate(name: String, config: Map[String, ParamResolver])

object ConfigUtil {
  lazy val configurations: Map[String, Map[String, String]] = environments.map { env =>
    val properties = new Properties
    properties.load(ClassLoader.getSystemClassLoader.getResourceAsStream(s"conf/${env}.properties"))
    env -> Map(properties.asScala.map { case (propName: String, propValue: String) =>
      propName -> predefined.foldLeft(propValue) { case (value: String, (r: Regex, sub: String)) =>
        r.replaceAllIn(value, sub)
      }
    }.toList: _*)
    // or
    // env -> properties.asScala.toMap
  }.toMap
  lazy val app1 = ApplicationPropertiesTemplate("app1", Map(
    "config.yaml" -> PlainStringResolver,
    "subdir/job.properties" -> PropertiesResolver
  ))
  lazy val springBootWebApp = ApplicationPropertiesTemplate("app2", Map(
    "ext.properties" -> PropertiesResolver
  ))
  val paramRegex = "(?<=\\$\\{)(.*?)(?=\\})".r
  val environments = List("env1", "env2", "env3")
  val predefined = Map(
    "\\$\\{macros1\\}".r -> "macros1Value",
    "\\$\\{macros1.1\\}".r -> "macros1.1Value",
    "\\$\\{macros2\\}".r -> "macros2Value",
    "\\$\\{macros3\\}".r -> "macros3Value"
  )

  //  lazy val configFiles = Map(
  //    "template/config1/config.yaml" -> PlainStringResolver,
  //    "template/config1/subdir/job.properties" -> PropertiesResolver
  //  )

  def main(args: Array[String]): Unit = {
    //    val props = new Properties()
    //    props.setProperty("key1", "value1")
    //    props.setProperty("key2", "value2")
    //    props.storeToXML(System.out, "test", "UTF-8")
    //    props.loadFromXML(new ByteArrayInputStream(
    //      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    //        "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">" +
    //        "<properties>" +
    //        "<key1>value1</key1>" +
    //        "<key2>value2</key2>" +
    //        "</properties>")
    //        .getBytes("UTF-8")))
    //    println(props)
    //    return

    //    val propValue = "${macros1}"
    //    println(predefined.foldLeft(propValue){case (value:String, (r: Regex, sub: String)) =>
    //      r.replaceAllIn(value, sub)
    //    })
    //    configurations.map{case (k,v) =>
    //        println(s"$k:")
    //        v.map(entry => println(s"\t${entry._1}: ${entry._2}"))
    //    }
    //    val s = "${sect1.prop11}/${sect1.prop12}"
    ////    println(paramRegex.findAllIn(s).toList)
    //    println(paramRegex.findAllIn(s).toList.foldLeft(s){ case(str, param) =>
    //        str.replaceAllLiterally(s"$${${param}}", conf(param))
    //    })
    //    "(?<=\\$\\{)[\\s\\S]+".r.findAllIn(s).map(println)
    //    "[\\w]+".r.findAllIn(s).foreach(println(_))
    //    val strs = for (s <- br.readLine()) yield s
    //    println(strs)
    //    println(Stream.continually[String](br.readLine()).takeWhile(_ != null).mkString)
    //    println(makeConfig(conf).mkString("\n"))

    //    makeConfig(configurations("env1")).map(println).force
    getConfig("env1", app1).map { case (str: String, stream: Stream[String]) =>
      println(s"\n===============$str==================")
      println(stream.force.mkString("\n"));
    }

    getConfig("env1", springBootWebApp).map { case (str: String, stream: Stream[String]) =>
      println(s"\n===============$str==================")
      println(stream.force.mkString("\n"));
    }

//    val v: IterableLike[Int, _] = Stream.from(1, 1).takeWhile(_ < 10)
//    val v2: IterableLike[String, _] = List("1", "2")
//
//    val v3 = Stream.from(1, 1).takeWhile(_ < 5)
//    v3.map(println)
//    println("^ result from lazy op")
//    v3.map(println).force
//    println("^ result from forced op")
  }

  def getConfig(env: String, app: ApplicationPropertiesTemplate): Map[String, Stream[String]] = {
    getConfig(configurations(env), app)
  }

  def getConfig(params: Map[String,String], app: ApplicationPropertiesTemplate): Map[String, Stream[String]] = {
    app.config.map { case (fileName, resolver) =>
      val br: BufferedReader = new BufferedReader(
        new InputStreamReader(ClassLoader.getSystemClassLoader.getResourceAsStream("template/" + app.name + "/" + fileName))
      )
      fileName -> resolver.replace(Stream.continually(
        Finalizer[String](br.readLine, _ == null, br.close)
      ).takeWhile(_ != null), params)
    }
  }

  private def replaceParams(s: String, conf: Map[String, String]): String = {
    paramRegex.findAllIn(s).foldLeft(s) { case (str, param) =>
      str.replaceAllLiterally(s"$${${param}}", conf(param))
    }
  }
}
