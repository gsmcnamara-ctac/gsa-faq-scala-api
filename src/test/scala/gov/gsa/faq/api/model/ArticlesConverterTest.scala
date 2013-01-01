package gov.gsa.faq.api.model

import org.scalatest.FreeSpec
import gov.gsa.faq.api.Constants
import collection.mutable.ArrayBuffer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import collection.immutable.Range.Double

@RunWith(classOf[JUnitRunner])
class ArticlesConverterTest extends FreeSpec {

  "ArticlesConverter" - {

    "toArticles should marhsall XML as Articles" in {
      val articlesConverter = new ArticlesConverter()
      val articles: Seq[Article] = articlesConverter.toArticles(Constants.XML_PATH)
      var article :Article = null
      articles.foreach{ _article =>
        if (_article.id=="11924") {
          article = _article
        }
      }
      assert("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=11924" == article.link, article.link)
      assert("* Employment: Foreign Nationals" == article.title, article.title)
      assert(217.53334 == article.rank, article.rank)
      assert("Nov 27 2012 07:17:14:000PM" == article.updated, article.updated)
      assert(article.body != null)
      val topics = article.topics.topic
      assert(2 == topics.size, topics.size)
      assert("International" == topics(0).name, topics(0).name)
      assert("Immigration and Naturalization" == topics(0).subtopics.subtopic(0), topics(0).subtopics.subtopic(0))
      assert("Jobs and Education" == topics(1).name, topics(1).name)
      assert("Workplace Issues" == topics(1).subtopics.subtopic(0), topics(1).subtopics.subtopic(0))
    }
  }
}

