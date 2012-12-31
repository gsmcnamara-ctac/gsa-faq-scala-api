package gov.gsa.faq

import org.apache.log4j.Logger

package object api {

  trait LogHelper {
    val loggerName = this.getClass.getName
    lazy val logger = Logger.getLogger(loggerName)
  }
}
