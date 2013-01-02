package gov.gsa.faq.api.dao

import gov.gsa.faq.api.LogHelper
import javax.sql.{DataSource}
import gov.gsa.rest.api.dao._
import gov.gsa.rest.api.model.{Sort, QueryParameter}
import gov.gsa.faq.api.model._
import scala.collection.JavaConversions._
import org.springframework.jdbc.core.JdbcTemplate
import org.apache.commons.lang.StringUtils
import collection.mutable.ListBuffer
import org.springframework.jdbc.support.rowset.SqlRowSet
import gov.gsa.faq.api.model.Article

class FaqDao(val dataSource: DataSource) extends LogHelper {

  val argumentsParser : ArgumentsParser = new ArgumentsParser()
  val queryParameter: QueryParameter = new QueryParameterImpl()
  val sort : Sort = new SortImpl()
  val allowedResultFilters = new ResultFilterImpl().getResultFilters()
  var jdbcTemplate : JdbcTemplate = new JdbcTemplate(dataSource)
  val sqlHelper : SQLHelper = new SQLHelper()

  def getArticles(queryParamString:String,resultFilterString:String,sortString:String) : Seq[Article] = {

    val queries = argumentsParser.getQueryList(queryParamString, queryParameter)
    val sorts = argumentsParser.getSorts(sortString)

    var validResultFilters : java.util.List[String] = null
    if(StringUtils.isEmpty(resultFilterString)) {
      validResultFilters = allowedResultFilters
    } else {
      validResultFilters = getValidResultFilters(resultFilterString)
    }

    if (queries.size == 0) {
      buildAllArticles(jdbcTemplate, validResultFilters, sorts)
    } else {

      queries.sortBy{query=>query.getName}

      var sql = "select * from articles where"
      var previousQueryName : String = null

      for (i <- 0 until queries.size()) {
        val query = queries(i)

        val currentQueryName = query.getName.toUpperCase
        var nextQueryName : String = null

        if(i+1 != queries.size) {
          nextQueryName = queries(i+1).getName.toUpperCase
        }

        if(currentQueryName != previousQueryName && currentQueryName == nextQueryName) {
          sql = sql + " (" + makeComparisonString(sql, query, currentQueryName) + " OR"
        } else if(currentQueryName == previousQueryName && currentQueryName == nextQueryName) {
          sql = sql + makeComparisonString(sql, query, currentQueryName) + " OR"
        } else if(currentQueryName == previousQueryName && currentQueryName != nextQueryName) {
          sql = sql + makeComparisonString(sql, query, currentQueryName) + " ) AND"
        } else {
          sql = sql + makeComparisonString(sql, query, currentQueryName) + " AND"
        }

        previousQueryName = currentQueryName
      }

      sql = sql.dropRight(" AND".length)
      sql = sql + sqlHelper.buildOrderBy(sorts,sort)

      val queryValues = new ListBuffer[Object]()
      for(query <- queries) {

        var queryValue = query.getValue
        if(query.getOperation == Operation.LIKE) {
          queryValues += queryValue.replaceAll("[*]","%")
        } else {
          queryValues += queryValue
        }
      }

      sql = modifySqlForTopicsQueries(sql)
      sql = modifySqlForRankQueries(sql)
      sql = modifySqlForSubtopicsQueries(sql)

      logger.info("Executing sql statement '" + sql + "'")

      val articles = new ListBuffer[Article]()
      val articlesRowSet = JdbcTemplateUtils.queryForRowSet(jdbcTemplate, sql, queryValues.toArray)
      while(articlesRowSet.next()) {
        articles += mapRowToArticle(jdbcTemplate, articlesRowSet, validResultFilters)
      }

      articles
    }
  }

  def modifySqlForTopicsQueries(sql:String) : String = {
    var _sql = sql
    _sql = _sql.replaceAll("UPPER[(]TOPIC[)] LIKE UPPER[(][?][)]", "ID IN (select distinct article from topics where UPPER(NAME) LIKE UPPER(?))")
    _sql = _sql.replaceAll("UPPER[(]TOPIC[)] > UPPER[(][?][)]", "ID IN (select distinct article from topics where UPPER(NAME) > UPPER(?))")
    _sql = _sql.replaceAll("UPPER[(]TOPIC[)] < UPPER[(][?][)]", "ID IN (select distinct article from topics where UPPER(NAME) < UPPER(?))")
    _sql
  }

  def modifySqlForSubtopicsQueries(sql:String) : String = {
    var _sql = sql
    _sql = _sql.replaceAll("UPPER[(]SUBTOPIC[)] LIKE UPPER[(][?][)]", "ID IN (select distinct article from subtopics where UPPER(SUBTOPIC) LIKE UPPER(?))")
    _sql = _sql.replaceAll("UPPER[(]SUBTOPIC[)] > UPPER[(][?][)]", "ID IN (select distinct article from subtopics where UPPER(SUBTOPIC) > UPPER(?))")
    _sql = _sql.replaceAll("UPPER[(]SUBTOPIC[)] < UPPER[(][?][)]", "ID IN (select distinct article from subtopics where UPPER(SUBTOPIC) < UPPER(?))")
    _sql
  }

  def modifySqlForRankQueries(sql:String) : String = {
    var _sql = sql
    _sql = _sql.replaceAll("UPPER[(]RANK[)] LIKE UPPER[(][?][)]", "RANK = ?")
    _sql = _sql.replaceAll("UPPER[(]RANK[)] > UPPER[(][?][)]", "RANK > ?")
    _sql = _sql.replaceAll("UPPER[(]RANK[)] < UPPER[(][?][)]", "RANK < ?")
    _sql = _sql.replaceAll("UPPER[(]RANK[)]", "RANK")
    _sql
  }

  def makeComparisonString(sql:String, query:Query, currentQueryName:String) : String = {
    " UPPER(" + currentQueryName + ")" + query.getOperation() + "UPPER(?)"
  }

  def buildAllArticles(jdbcTemplate:JdbcTemplate,resultFilters:java.util.List[String],sorts:java.util.List[String]) : Seq[Article] = {

    val sql = "select * from articles" + sqlHelper.buildOrderBy(sorts,sort)

    logger.info("Executing the sql statement '" + sql + "'")

    val rowSet = jdbcTemplate.queryForRowSet(sql)
    var articles = new ListBuffer[Article]()
    while(rowSet.next()) {
      articles += mapRowToArticle(jdbcTemplate, rowSet, resultFilters)
    }

    articles
  }

  def mapRowToArticle(jdbcTemplate:JdbcTemplate, rowSet:SqlRowSet, resultFilters:java.util.List[String]) : Article = {

    val articleId = rowSet.getString("id")

    var id : String = null
    if (resultFilters.contains("id")) {
      id = articleId
    }

    var link : String = null
    if (resultFilters.contains("link")) {
      link = rowSet.getString("link")
    }

    var title : String = null
    if (resultFilters.contains("title")) {
      title = rowSet.getString("title")
    }

    var body : String = null
    if (resultFilters.contains("body")) {
      body = "<![CDATA["+rowSet.getString("body")+"]]"
    }

    var rank : String = null
    if (resultFilters.contains("rank")) {
      rank = rowSet.getDouble("rank").toString
    }

    var updated : String = null
    if (resultFilters.contains("updated")) {
      updated = rowSet.getString("updated")
    }

    var topics : Topics = null
    if (resultFilters.contains("topic")) {

      val topicsRowSet = jdbcTemplate.queryForRowSet("select * from topics where article=?", articleId)
      var topicList = new ListBuffer[Topic]()

      while(topicsRowSet.next()) {

        val topicId = topicsRowSet.getString("id")
        val topicName = topicsRowSet.getString("name")

        val subtopicRowSet = jdbcTemplate.queryForRowSet("select * from subtopics where topic=?", topicId)
        var subtopicList = new ListBuffer[String]()

        while(subtopicRowSet.next()) {
          subtopicList += subtopicRowSet.getString("subtopic")
        }

        var topic : Topic = null
        if (subtopicList.isEmpty) {
          topic = Topic(topicName,null)
        } else {
          topic = Topic(topicName,Subtopics(subtopicList))
        }

        topicList += topic
      }

      if (!topicList.isEmpty) {
        topics = new Topics(topicList)
      }
    }

    new Article(id,link,title,body,rank,updated,topics)
  }

  def getValidResultFilters(resultFilterString: String) = {
    val resultFilters = argumentsParser.getResultFilters(resultFilterString)
    val validResultFilters = new ListBuffer[String]()
    for(resultFilter <- resultFilters) {
      if (allowedResultFilters.contains(resultFilter)) {
        validResultFilters += resultFilter
      }
    }
    validResultFilters
  }
}
