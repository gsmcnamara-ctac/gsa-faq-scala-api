package gov.gsa.faq.api.cms

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import com.ctacorp.rhythmyx.soap.PercussionContentServices
import gov.gsa.faq.api.model.{Subtopics, Topic, Topics, Article}
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer

@RunWith(classOf[JUnitRunner])
class ArticlesCmsServicesTest extends FeatureSpec with BeforeAndAfter {

  val services = new ArticlesCmsServices()

  feature("createArticle") {

    scenario("article has all fields, two topics each with 2 subtopics") {

    }
  }

  feature("makeTopicsString") {

    scenario("two topics with two subtopics each") {

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1",new Subtopics(List("subtopic1","subtopic2")))
      topicList += new Topic("topic2",new Subtopics(List("subtopic3","subtopic4")))
      topics.topic = topicList.toList

      assert("topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4" == services.makeTopicsString(topics), services.makeTopicsString(topics))
    }
  }
}

class ArticlesCmsServices extends PercussionContentServices {

   var services : PercussionContentServices = this

    def createArticle(article:Article, targetFolder:String) : String = {

      var fields = Map[String,Object]()
      fields += ("id" -> article.id)
      fields += ("link" -> article.link)
      fields += ("article_title" -> article.title)
      fields += ("body" -> article.body.replaceFirst("<![CDATA[","").dropRight("]]".length))
      fields += ("rank" -> article.rank)
      fields += ("updated" -> article.updated)
      fields += ("topics_subtopics" -> makeTopicsString(article.topics))

      services.createItem(fields, targetFolder, "faqArticle").toString
    }

    def makeTopicsString (topics:Topics) : String = {
      var topicList = topics.topic
      var topicsString : String = null
      for(topic <- topicList) {
        topicsString = topic.name
        var subtopics = topic.subtopics

      }

      null
    }
}
