package gov.gsa.faq.api.dao

import gov.gsa.rest.api.dao.{XMLUnmarshallUtils, DatabaseAdministrator}
import gov.gsa.faq.api.model.Root
import gov.gsa.faq.api.{Constants, LogHelper}
import javax.sql.DataSource
import org.springframework.jdbc.core.JdbcTemplate

class FaqDatabase extends DatabaseAdministrator with LogHelper {

  val xmlUnmarshallUtils = new XMLUnmarshallUtils[Root](classOf[Root])

  def clearTables(dataSource: DataSource) {
    var jdbcTemplate = new JdbcTemplate(dataSource)
  }

  def loadTables(dataSource: DataSource) {
    var jdbcTemplate = new JdbcTemplate(dataSource)
  }

  def hasRecords(dataSource: DataSource) : Boolean = {
    var jdbcTemplate = new JdbcTemplate(dataSource)
    return false
  }

  def getTableNames(): Array[String] = {
    Array("articles", "topics", "subtopics")
  }

  def getXmlRecordsPaths(): Array[String] = {
    Constants.XML_PATHS
  }

  def getDatabaseName(): String = {
    "faq"
  }

  def getTableCreationSqls(): Array[String] = {
    val sqls = new Array[String](3)
    sqls(0) = "create table articles (id VARCHAR not null, link VARCHAR, title VARCHAR, body VARCHAR, rank DOUBLE, updated VARCHAR, PRIMARY KEY (id))"
    sqls(1) = "create table topics (id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, article VARCHAR, name VARCHAR)"
    sqls(2) = "create table subtopics (id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, article VARCHAR, topic INTEGER, subtopic VARCHAR)"
    return sqls
  }
}
