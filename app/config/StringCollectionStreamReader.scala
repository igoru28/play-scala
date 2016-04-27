package config

import java.io.{InputStreamReader, BufferedReader}
import java.util.{Properties, Arrays}

/**
 * Author igor on 13.02.16.
 */
case class StringCollectionStreamReader(stream: Stream[String]) extends java.io.Reader {
  override def close(): Unit = {}
  private val iterator = stream.iterator
  private var currentLine = iterator.next()

  override def read(cbuf: Array[Char], offset: Int, length: Int): Int = {
    val off: Int = offset
    val end: Int = offset + length
    if (offset < 0 || offset > cbuf.length || length < 0 || end < 0 || end > cbuf.length) {
      throw new IndexOutOfBoundsException
    }

    while (currentLine.length < length && iterator.hasNext) {
      currentLine += s"\n${iterator.next()}"
    }

    val resultLength = math min(currentLine.length, length)

    if (resultLength > 0) {
      Array.copy(currentLine.toCharArray, 0, cbuf, off, resultLength)
      if (currentLine.length > resultLength) {
        currentLine = currentLine.substring(resultLength)
      } else {
        currentLine = ""
      }
    }
    return resultLength
  }
}

object Test extends App {
  val br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader.getResourceAsStream("conf/env1.properties")))
  val stream = Stream.continually(br.readLine()).takeWhile(_ != null)
  val properties = new Properties()
  properties.load(StringCollectionStreamReader(stream))
  println(properties)
  properties.load(StringCollectionStreamReader(stream))
  println(properties)
  properties.load(StringCollectionStreamReader(stream))
  println(properties)
}