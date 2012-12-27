package gov.gsa.faq.api.dao

import gov.gsa.rest.api.dao.{XMLUnmarshallUtils, DatabaseAdministrator}
import gov.gsa.faq.api.model.Root
import gov.gsa.faq.api.{Constants, LogHelper}
import javax.sql.DataSource
import org.springframework.jdbc.core.JdbcTemplate

class FaqDatabase extends DatabaseAdministrator with LogHelper {

  val xmlUnmarshallUtils = new XMLUnmarshallUtils[Root](classOf[Root])

  def getXmlRecordsPaths(): Array[String] = { Constants.XML_PATHS }

  def getDatabaseName(): String = { "faq" }

  def getTableNames(): Array[String] = { Array("articles", "topics", "subtopics") }

  def getTableCreationSqls(): Array[String] = {
    Array(
      "create table articles (id VARCHAR not null, link VARCHAR, title VARCHAR, body VARCHAR, rank DOUBLE, updated VARCHAR, PRIMARY KEY (id))",
      "create table topics (id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, article VARCHAR, name VARCHAR)",
      "create table subtopics (id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, article VARCHAR, topic INTEGER, subtopic VARCHAR)"
    )
  }

  def clearTables(dataSource: DataSource) {
    var jdbcTemplate = new JdbcTemplate(dataSource)
    for(tableName <- getTableNames()) {
      jdbcTemplate.execute("delete from " + tableName)
    }
  }

  def loadTables(dataSource: DataSource) {
    var jdbcTemplate = new JdbcTemplate(dataSource)
    for(xmlPath <- getXmlRecordsPaths()) {

    }
  }

  def hasRecords(dataSource: DataSource) : Boolean = {
    if (new JdbcTemplate(dataSource).queryForInt("select count(*) from articles") > 0) true else false
  }
}
