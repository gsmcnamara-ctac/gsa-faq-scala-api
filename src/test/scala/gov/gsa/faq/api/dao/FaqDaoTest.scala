package gov.gsa.faq.api.dao

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FeatureSpec}
import org.apache.commons.io.FileUtils
import java.io.File
import gov.gsa.faq.api.Constants
import gov.gsa.rest.api.dao.{ArgumentsParser, InMemoryHSQLDatabase}
import gov.gsa.faq.api.model.{QueryParameterImpl, Article, Articles}
import gov.gsa.rest.api.model.QueryParameter
import javax.sql.DataSource
import scala.collection.JavaConversions._

class FaqDaoTest extends FeatureSpec with BeforeAndAfter {

  var database : InMemoryHSQLDatabase = _
  var faqDao : FaqDao = _
  var dataSource : DataSource = _

  before {

    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_EN.xml"), new File(Constants.XML_PATH))
    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_ES.xml"), new File(Constants.XML_PATH_ES))

    val faqDatabase = new FaqDatabase()
    database = InMemoryHSQLDatabase.getInstance(faqDatabase)
    val dataSource = database.getDataSource()
    faqDatabase.clearTables(dataSource)

    faqDao = new FaqDao(dataSource)
  }

  feature("getArticles") {

    scenario("get all the articles from the database") {

    }
  }
}

class FaqDao(

  val dataSource: DataSource

) {

  val argumentsParser : ArgumentsParser = new ArgumentsParser()
  val queryParameter: QueryParameter = new QueryParameterImpl()

  def getArticles(queryParamString:String,resultFilterString:String,sortString:String) : Seq[Article] = {

    val queries = argumentsParser.getQueryList(queryParamString, queryParameter)
    queries.foreach { query =>

    }

    null
  }
}

//public List<Article> getArticles(String queryParamString, String resultFilterString, String sortString) throws SQLException {
//
//ArrayList<Article> articles = new ArrayList<Article>();
//
//List<Query> queries = argumentsParser.getQueryList(queryParamString, queryParameter);
//List<String> sorts = argumentsParser.getSorts(sortString);
//
//List<String> validResultFilters;
//if (StringUtils.isEmpty(resultFilterString)) {
//validResultFilters = allowedResultFilters;
//} else {
//validResultFilters = getValidResultFilters(resultFilterString);
//}
//
//if (queries.size() == 0) {
//return buildAllArticles(jdbcTemplate, validResultFilters, sorts);
//}
//
//Collections.sort(queries);
//
//StringBuilder sqlBuilder = new StringBuilder();
//sqlBuilder.append("select * from articles where");
//
//String previousQueryName = new String();
//for (int i = 0; i < queries.size(); i++) {
//
//Query query = queries.get(i);
//
//String currentQueryName = query.getName().toUpperCase();
//String nextQueryName = new String();
//if (i + 1 != queries.size()) {
//nextQueryName = queries.get(i + 1).getName().toUpperCase();
//}
//
//if ((!currentQueryName.equals(previousQueryName)) && currentQueryName.equals(nextQueryName)) {
//sqlBuilder.append(" (");
//appendComparisonString(sqlBuilder, query, currentQueryName);
//sqlBuilder.append(" OR");
//} else if (currentQueryName.equals(previousQueryName) && currentQueryName.equals(nextQueryName)) {
//appendComparisonString(sqlBuilder, query, currentQueryName);
//sqlBuilder.append(" OR");
//} else if (currentQueryName.equals(previousQueryName) && !currentQueryName.equals(nextQueryName)) {
//appendComparisonString(sqlBuilder, query, currentQueryName);
//sqlBuilder.append(" )");
//sqlBuilder.append(" AND");
//} else {
//appendComparisonString(sqlBuilder, query, currentQueryName);
//sqlBuilder.append(" AND");
//}
//
//previousQueryName = currentQueryName;
//}
//
//sqlBuilder.replace(sqlBuilder.length() - " AND".length(), sqlBuilder.length(), "");
//
//sqlBuilder.append(buildOrderBy(sorts));
//
//String sql = sqlBuilder.toString();
//
//List<String> queryValues = new ArrayList<String>();
//for (Query query : queries) {
//
//String queryValue = query.getValue();
//if (query.getOperation().equals(Operation.LIKE)) {
//queryValues.add(queryValue.replaceAll("[*]", "%"));
//} else {
//queryValues.add(queryValue);
//}
//}
//
//sql = modifySqlForTopicsQueries(sql);
//sql = modifySqlForRankQueries(sql);
//sql = modifySqlForSubtopicsQueries(sql);
//
//log.info(new StringBuilder("Executing sql statement '").append(sql).append("'").toString());
//
//SqlRowSet articlesRowSet = jdbcTemplate.queryForRowSet(sql, queryValues.toArray());
//while (articlesRowSet.next()) {
//Article article = mapRowToContact(jdbcTemplate, articlesRowSet, validResultFilters);
//articles.add(article);
//}
//
//return articles;
//}