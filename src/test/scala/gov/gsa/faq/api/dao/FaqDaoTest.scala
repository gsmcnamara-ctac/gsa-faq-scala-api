package gov.gsa.faq.api.dao

import org.scalatest.{BeforeAndAfter, FeatureSpec}
import org.apache.commons.io.FileUtils
import java.io.File
import io.Source
import gov.gsa.faq.api.Constants
import gov.gsa.rest.api.dao._
import javax.sql.DataSource
import gov.gsa.faq.api.model.Article
import scala.collection.JavaConversions._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
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

    faqDao = new FaqDao(dataSource)
  }

  feature("getArticles") {

    scenario("all null params (return all articles all data)") {

      val articles = faqDao.getArticles(null,null,null)
      assert(2020 == articles.size, articles.size)

      var article : Article = null
      articles.foreach { _article =>
        if(_article.id == "9666") {
          article = _article
        }
      }

      assert(article != null)
      assert("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=9666" == article.link, article.link)
      assert(article.title.matches("Fish and Wildlife Service:.*Student Employment Programs"))
      assert("<![CDATA["+Source.fromInputStream(getClass().getResourceAsStream("/9666.body")).getLines().mkString("\n")+"]]" == article.body, article.body)
      assert(50.43334 == article.rank.toDouble, article.rank)
      assert("Nov 26 2012 04:58:24:000PM" == article.updated, article.updated)

      val topics = article.topics.topic
      assert(2 == topics.size, topics.size)
      assert("Jobs and Education" == topics(0).name)
      assert("Fish and Wildlife Service (FWS)" == topics(1).name)

      assert("Education" == topics(0).subtopics.subtopic(0))
      assert("Jobs" == topics(0).subtopics.subtopic(1))
    }

    scenario("with all results filters") {

      val articles = faqDao.getArticles(null, "id|link|title|body|rank|updated|topic", null)
      assert(2020 == articles.size)

      var article : Article = null
      articles.foreach { _article =>
        if(_article.id == "9666") {
          article = _article
        }
      }

      assert(article != null)
      assert("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=9666" == article.link, article.link)
      assert(article.title.matches("Fish and Wildlife Service:.*Student Employment Programs"))
      assert("<![CDATA["+Source.fromInputStream(getClass().getResourceAsStream("/9666.body")).getLines().mkString("\n")+"]]" == article.body, article.body)
      assert(50.43334d == article.rank.toDouble, article.rank)
      assert("Nov 26 2012 04:58:24:000PM" == article.updated, article.updated)

      val topics = article.topics.topic
      assert(2 == topics.size, topics.size)
      assert("Jobs and Education" == topics(0).name)
      assert("Fish and Wildlife Service (FWS)" == topics(1).name)

      assert("Education" == topics(0).subtopics.subtopic(0))
      assert("Jobs" == topics(0).subtopics.subtopic(1))
    }

    scenario("only 'id' result filter") {

      val articles = faqDao.getArticles(null, "id", null)
      assert(2020 == articles.size)

      var article : Article = null
      articles.foreach { _article =>
        if(_article.id == "9666") {
          article = _article
        }
      }

      assert(article!=null)

      assert(null==article.link)
      assert(null==article.title)
      assert(null==article.body)
      assert(null==article.rank)
      assert(null==article.updated)
      assert(null==article.topics)
    }

    scenario("query by body") {
      val articles = faqDao.getArticles("body::*provide information on available state benefits*", null, null)
      assert(1==articles.size)
    }

    scenario("query by topic") {
      var articles = faqDao.getArticles("topic::Reference and General Government", null, null)
      assert(520==articles.size)
      articles = faqDao.getArticles("topic::1_Featured Content USA.gov INTERNAL", null, null)
      assert(10==articles.size)
    }

    scenario("query by subtopic") {
      val articles = faqDao.getArticles("subtopic::Taxes", null, null)
      assert(111==articles.size)
    }

    scenario("query by rank") {
      val articles = faqDao.getArticles("rank:gt:50.0", null, null)
      assert(1077==articles.size)
    }

    scenario("query by topic using prefix and sufix wildcard, use id|title|topic as result filter and sort by title") {
      val articles = faqDao.getArticles("topic::*social*", "id|title|topic", "title")

      var foundTopics = false
      for (article <- articles) {
        if(article.topics!=null) {
          foundTopics = true
        }
      }
      assert(foundTopics)
    }

    scenario("query by topic using prefix and sufix wildcard, use id|title|topic as result filter and sort by rank") {
      val articles = faqDao.getArticles("topic::*social*", "id|title|topic", "rank")

      var foundTopics = false
      for (article <- articles) {
        if(article.topics!=null) {
          foundTopics = true
        }
      }
      assert(foundTopics)
    }

    scenario("query by id") {
      val articles = faqDao.getArticles("id::9666", null, null)
      assert(1==articles.size)
    }

    scenario("sort by descending id") {
      val articles = faqDao.getArticles(null, null, "-id")

      assert(2020==articles.size)
      assert("9996"==articles(0).id)
      assert("10000"==articles(articles.size-1).id)
    }
  }
}

