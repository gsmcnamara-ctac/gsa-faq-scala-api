package gov.gsa.faq.api.dao

import gov.gsa.rest.api.dao.{XMLUnmarshallUtils, DatabaseAdministrator}
import gov.gsa.faq.api.model.{ArticlesConverter, Article, Root}
import gov.gsa.faq.api.{Constants, LogHelper}
import javax.sql.DataSource
import org.springframework.jdbc.core.{PreparedStatementCreator, JdbcTemplate}
import java.sql.{Statement, PreparedStatement, Connection}
import java.util.Collections
import org.springframework.jdbc.support.GeneratedKeyHolder

class FaqDatabase extends DatabaseAdministrator with LogHelper {

  def getXmlRecordsPaths(): Array[String] = {
    Constants.XML_PATHS
  }

  def getDatabaseName(): String = {
    "faq"
  }

  def getTableNames(): Array[String] = {
    Array("articles", "topics", "subtopics")
  }

  def getTableCreationSqls(): Array[String] = {
    Array(
      "create table articles (id VARCHAR not null, link VARCHAR, title VARCHAR, body VARCHAR, rank DOUBLE, updated VARCHAR, PRIMARY KEY (id))",
      "create table topics (id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, article VARCHAR, name VARCHAR)",
      "create table subtopics (id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, article VARCHAR, topic INTEGER, subtopic VARCHAR)"
    )
  }

  def clearTables(dataSource: DataSource) {
    var jdbcTemplate = new JdbcTemplate(dataSource)

    for (tableName <- getTableNames()) {
      val sql = "delete from " + tableName
      logger.info("Executing sql statement '" + sql + "'")
      jdbcTemplate.execute(sql)
    }
  }

  def loadTables(dataSource: DataSource) {
    val jdbcTemplate = new JdbcTemplate(dataSource)
    for (xmlPath <- getXmlRecordsPaths()) {

      logger.info("Loading tables with records from '" + xmlPath + "'")

      val articles = new ArticlesConverter().toArticles(xmlPath)

      for (article: Article <- articles) {

        jdbcTemplate.update(new PreparedStatementCreator {
          def createPreparedStatement(connection: Connection): PreparedStatement = {
            val ps: PreparedStatement = connection.prepareStatement("insert into articles (id, link, title, body, rank, updated) values (?,?,?,?,?,?) ")

            val articleId = article.id

            ps.setString(1, articleId)
            ps.setString(2, article.link)
            ps.setString(3, article.title)
            ps.setString(4, article.body)
            ps.setDouble(5, article.rank.toDouble)
            ps.setString(6, article.updated)

            if (article.topics != null && article.topics.topic != null) {
              for (topic <- article.topics.topic) {
                val keyHolder = new GeneratedKeyHolder()
                new JdbcTemplate(dataSource).update(new PreparedStatementCreator {
                  def createPreparedStatement(connection: Connection): PreparedStatement = {
                    val ps = connection.prepareStatement("insert into topics (article, name) values (?,?)", Statement.RETURN_GENERATED_KEYS)
                    ps.setString(1, articleId)
                    ps.setString(2, topic.name)
                    ps
                  }
                }, keyHolder)
                if (topic.subtopics != null && topic.subtopics != null) {
                  for (subtopic <- topic.subtopics.subtopic) {
                    new JdbcTemplate(dataSource).update(new PreparedStatementCreator {
                      def createPreparedStatement(connection: Connection): PreparedStatement = {
                        val ps = connection.prepareStatement("insert into subtopics (article, topic, subtopic) values (?,?,?)", Statement.RETURN_GENERATED_KEYS)
                        val key = keyHolder.getKey
                        ps.setString(1, articleId)
                        ps.setInt(2, key.intValue())
                        ps.setString(3, subtopic)
                        ps
                      }
                    })
                  }
                }
              }
            }
            ps
          }
        })
      }
    }
  }

  def hasRecords(dataSource: DataSource): Boolean = {
    val sql = "select count(*) from articles"
    logger.info("Executing sql statement '" + sql + "'")
    if (new JdbcTemplate(dataSource).queryForInt(sql) > 0) true else false
  }
}
