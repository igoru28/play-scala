package config

import java.util.Properties
import StreamParamReplacer._

/**
 * Author igor on 13.02.16.
 */

trait StreamParamReplacer {

  def replace(stream: Stream[String], staticPropertiesMap: Map[String,String]): Stream[String]
}

object StreamParamReplacer {
  val paramRegex = "(?<=\\$\\{)(.*?)(?=\\})".r
}


trait ParamResolver {
  private[config] def resolve(property: String, staticPropertiesMap: Map[String,String]):String = {
    staticPropertiesMap(property)
  }
  def replace(stream: Stream[String], staticPropertiesMap: Map[String,String]): Stream[String] = {
    stream.map[String,Stream[String]] { line =>
      paramRegex.findAllIn(line).foldLeft(line){ case (str, param) =>
        str.replaceAllLiterally(s"$${${param}}", resolve(param, staticPropertiesMap))
      }
    }
  }
}

object PlainStringResolver extends ParamResolver

object PropertiesResolver extends ParamResolver {
  val properties = new Properties()

  private[config] override def resolve(property: String, staticPropertiesMap: Map[String,String]):String = {
    resolve(property, staticPropertiesMap, Set())
  }

  private def resolve(property: String, staticPropertiesMap: Map[String,String], propertyVisitor: Set[String]):String = {
    if (propertyVisitor.contains(property)) {
      throw new IllegalStateException(s"Property $property has cyclic dependency")
    }
    val patternValue = Option(properties.getProperty(property)).getOrElse(staticPropertiesMap(property))
    val value = paramRegex.findAllIn(patternValue).foldLeft(patternValue){case (str, param) =>
      str.replaceAllLiterally(s"$${${param}}", resolve(param, staticPropertiesMap, propertyVisitor + property))
    }
    properties.put(property, value)
    value
  }

  override def replace(stream: Stream[String], staticPropertiesMap: Map[String,String]): Stream[String] = {
    properties.load(StringCollectionStreamReader(stream))
    super.replace(stream, staticPropertiesMap)
  }
}
