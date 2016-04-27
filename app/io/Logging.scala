package io

import play.api.Logger

/**
 * Author igor on 22.04.16.
 */
trait Logging {
  protected val logger = Logger(this.getClass)
}
