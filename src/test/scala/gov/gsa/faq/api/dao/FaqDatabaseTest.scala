package gov.gsa.faq.api.dao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, GivenWhenThen, BeforeAndAfter}
import io.Source
import org.apache.commons.io.FileUtils
import java.io.File
import gov.gsa.faq.api.Constants
import gov.gsa.rest.api.dao.InMemoryHSQLDatabase
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.rowset.SqlRowSet

@RunWith(classOf[JUnitRunner])
class FaqDatabaseTest extends FeatureSpec with BeforeAndAfter {

  var database: InMemoryHSQLDatabase = _

  before {

    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_EN.xml"), new File(Constants.XML_PATH))
    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_ES.xml"), new File(Constants.XML_PATH_ES))

    val faqDatabase = new FaqDatabase()
    database = InMemoryHSQLDatabase.getInstance(faqDatabase)
    faqDatabase.clearTables(database.getDataSource)
  }

  feature("loadTables") {

    scenario("load all English and Spanish records form the XML files") {

      val jdbcTemplate = new JdbcTemplate(database.getDataSource)

      assert(2020 == jdbcTemplate.queryForInt("select count(*) from articles"))

      var rowSet = jdbcTemplate.queryForRowSet("select * from articles where id='9666'")
      assert(rowSet.next())
      assert("9666" == rowSet.getString("id"), rowSet.getString("id"))
      assert("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=9666" == rowSet.getString("link"), rowSet.getString("link"))
      assert(rowSet.getString("title").matches("Fish and Wildlife Service:.*Student Employment Programs"), rowSet.getString("title"))
      assert(50.43334 == rowSet.getDouble("rank"), rowSet.getDouble("rank"))
      assert("Nov 26 2012 04:58:24:000PM" == rowSet.getString("updated"), rowSet.getString("updated"))
      assert(Source.fromInputStream(getClass().getResourceAsStream("/9666.body")).getLines().mkString("\n") == rowSet.getString("body"), rowSet.getString("body"))

      rowSet = jdbcTemplate.queryForRowSet("select * from topics where article='9666'")
      assert(rowSet.next())
      assert("Jobs and Education" == rowSet.getString("name"), rowSet.getString("name"))
      val topicId = rowSet.getInt("id")
      assert(rowSet.next())
      assert("Fish and Wildlife Service (FWS)" == rowSet.getString("name"), rowSet.getString("name"))
      assert(!rowSet.next())

      rowSet = jdbcTemplate.queryForRowSet("select * from subtopics where topic="+topicId)
      assert(rowSet.next())
      assert("Education" == rowSet.getString("subtopic"))
      assert(rowSet.next())
      assert("Jobs" == rowSet.getString("subtopic"))
    }
  }
}
