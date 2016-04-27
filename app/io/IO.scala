package io

import java.io._

import scala.collection.TraversableOnce
import scala.util.{Failure, Success, Try}

/**
 * Author igor on 16.04.16.
 */
object IO {
  def apply(in: InputStream): Iterator[Int] = {
    Iterator.continually(Finalizer[Int](in.read, _ < 0, in.close)).takeWhile(_ >= 0)
  }

  def apply(in: BufferedReader): Stream[String] = {
    Stream.continually(Finalizer[String](in.readLine, _ != null, in.close)).takeWhile(_ != null)
  }

  def writeIterator[A](dest: String, source: TraversableOnce[A], write: (A, OutputStream) => Unit) = {
    val file = new File(dest)
    if (!file.exists()) {
      if (!file.getParentFile.exists()) {
        if (!file.getParentFile.mkdirs()) {
          throw new IllegalStateException(s"Cannot create parent directory ${file.getParent}")
        }
      }
      val out = new BufferedOutputStream(new FileOutputStream(file))
      try {
        source.foreach(el => write(el, out))
      } finally {
        out.close()
      }
    }

  }
}

object Finalizer {
  def apply[A](readOp: () => A, closeCondition: A => Boolean, closeOp: () => Unit): A = {
    Try(readOp()) match {
      case Success(value) =>
        if (closeCondition(value)) {
          closeOp()
        }
        value
      case Failure(e) =>
        closeOp()
        throw e
    }
  }
}