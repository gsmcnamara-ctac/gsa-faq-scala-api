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

@RunWith(classOf[JUnitRunner])
class FaqDatabaseSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfter {

  var database : InMemoryHSQLDatabase = _

  before {

    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("FAQ_EN.xml"), new File(Constants.XML_PATH))
    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("FAQ_ES.xml"), new File(Constants.XML_PATH_ES))

    val faqDatabase = new FaqDatabase()
    database = InMemoryHSQLDatabase.getInstance(faqDatabase)
    faqDatabase.clearTables(database.getDataSource)
  }

  feature("loadTables") {

    scenario("load all English and Spanish records form the XML files") {

      val jdbcTemplate = new JdbcTemplate(database.getDataSource);

    }

//    DataSource dataSource = database.getDataSource();
//    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
//    int contactCount = jdbcTemplate.queryForInt("select count(*) from articles");
//    assertEquals(2020, contactCount);
//
//    SqlRowSet rowSet = jdbcTemplate.queryForRowSet("select * from articles where id='9666'");
//    assertTrue(rowSet.next());
//    assertEquals("9666", rowSet.getString("id"));
//    assertEquals("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=9666", rowSet.getString("link"));
//    assertTrue(rowSet.getString("title").matches("Fish and Wildlife Service:.*Student Employment Programs"));
//    assertEquals(IOUtils.toString(new ClassPathResource("9666.body").getInputStream()), rowSet.getString("body"));
//    assertEquals("50.43334", rowSet.getString("rank"));
//    assertEquals("Nov 26 2012 04:58:24:000PM", rowSet.getString("updated"));
//
//    rowSet = jdbcTemplate.queryForRowSet("select * from topics where article='9666'");
//    assertTrue(rowSet.next());
//    assertEquals("9666", rowSet.getString("article"));
//    assertEquals("Jobs and Education", rowSet.getString("name"));
//    int topicId = rowSet.getInt("id");
//    assertTrue(rowSet.next());
//    assertEquals("9666", rowSet.getString("article"));
//    assertEquals("Fish and Wildlife Service (FWS)", rowSet.getString("name"));
//    assertFalse(rowSet.next());
//
//    rowSet = jdbcTemplate.queryForRowSet("select * from subtopics where topic="+topicId);
//    assertTrue(rowSet.next());
//    assertEquals(""+topicId, rowSet.getString("topic"));
//    assertEquals("Education", rowSet.getString("subtopic"));
//    assertTrue(rowSet.next());
//    assertEquals(""+topicId, rowSet.getString("topic"));
//    assertEquals("Jobs", rowSet.getString("subtopic"));
  }
}
