package gov.gsa.faq.api.cms

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import com.ctacorp.rhythmyx.soap.{RhythmyxContentTypeFieldUtils, ServicesConnector, PercussionContentServices}
import gov.gsa.faq.api.model.{Subtopics, Topic, Topics, Article}
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer
import org.mockito.Mockito._
import com.percussion.webservices.content.{PSFieldValue, PSField, PSItem, PSItemSummary}
import gov.gsa.faq.api.LogHelper
import com.percussion.webservices.common.Reference

@RunWith(classOf[JUnitRunner])
class ArticlesCmsServicesTest extends FeatureSpec with BeforeAndAfter {

  val services = new ArticlesCmsServices()
  var percussionServices = mock(classOf[PercussionContentServices])
  var servicesConnector = mock(classOf[ServicesConnector])

  before {
    services.services = percussionServices
    services.servicesConnector = servicesConnector
    reset(percussionServices,servicesConnector)
  }

  feature("createArticle") {

    scenario("article has all fields, two topics each with 2 subtopics") {

      var fields = Map[String,Object]()
      fields += ("id" -> "id")
      fields += ("link" -> "link")
      fields += ("article_title" -> "title")
      fields += ("body" -> "body")
      fields += ("rank" -> "rank")
      fields += ("updated" -> "updated")
      fields += ("topics_subtopics" -> "topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")

      val article = new Article()
      article.body = "<![CDATA[[body]]"
      article.id = "id"
      article.link = "link"
      article.rank = "rank"
      article.title = "title"
      article.updated = "updated"

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1",new Subtopics(List("subtopic1","subtopic2")))
      topicList += new Topic("topic2",new Subtopics(List("subtopic3","subtopic4")))
      topics.topic = topicList.toList

      article.topics = topics

      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolder"))
      when(percussionServices.createItem(fields, "targetFolder", "faqArticle")).thenReturn(1234)

      assert("1234"===services.createArticle(article))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services)
      verify(percussionServices).logout()
    }
  }

  feature("getAll") {

    scenario("load 1 items from the CMS and return a List of 1 Articles") {

      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolder"))
      val summary: PSItemSummary = mock(classOf[PSItemSummary])
      val summaries: Array[PSItemSummary] = Array[PSItemSummary](summary)
      when(percussionServices.findFolderChildren("targetFolder")).thenReturn(summaries)

      val reference: Reference = mock(classOf[Reference])
      when(reference.getName).thenReturn("faqArticle")

      when(summary.getContentType).thenReturn(reference)

      val item: PSItem = mock(classOf[PSItem])
      when(summary.getId).thenReturn(1234)

      val idField = new PSField()
      idField.setName("id")
      val idValue = new PSFieldValue()
      idValue.setRawData("id")
      idField.setPSFieldValue(Array[PSFieldValue](idValue))

      val linkField = new PSField()
      linkField.setName("link")
      val linkValue = new PSFieldValue()
      linkValue.setRawData("link")
      linkField.setPSFieldValue(Array[PSFieldValue](linkValue))

      val titleField = new PSField()
      titleField.setName("article_title")
      val titleValue = new PSFieldValue()
      titleValue.setRawData("title")
      titleField.setPSFieldValue(Array[PSFieldValue](titleValue))

      val bodyField = new PSField()
      bodyField.setName("body")
      val bodyValue = new PSFieldValue()
      bodyValue.setRawData("body")
      bodyField.setPSFieldValue(Array[PSFieldValue](bodyValue))

      val rankField = new PSField()
      rankField.setName("rank")
      val rankValue = new PSFieldValue()
      rankValue.setRawData("rank")
      rankField.setPSFieldValue(Array[PSFieldValue](rankValue))

      val updatedField = new PSField()
      updatedField.setName("updated")
      val updatedValue = new PSFieldValue()
      updatedValue.setRawData("updated")
      updatedField.setPSFieldValue(Array[PSFieldValue](updatedValue))

      val topicsField = new PSField()
      topicsField.setName("topics_subtopics")
      val topicsValue: PSFieldValue = new PSFieldValue()
      topicsValue.setRawData("topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")
      topicsField.setPSFieldValue(Array(topicsValue))

      when(item.getFields).thenReturn(Array[PSField](idField,linkField,titleField,bodyField,rankField,updatedField,topicsField))

      when(percussionServices.loadItem(1234)).thenReturn(item)

      val articles = services.getAll()
      assert(1===articles.size)
      assert("id"===articles(0).id)
      assert("link"===articles(0).link)
      assert("body"===articles(0).body)
      assert("rank"===articles(0).rank)
      assert("title"===articles(0).title)
      assert("updated"===articles(0).updated)

      val topics = articles(0).topics
      assert(2===topics.topic.size)
      assert("topic1"===topics.topic.get(0).name)
      assert(2===topics.topic.get(0).subtopics.subtopic.size)
      assert("subtopic1"===topics.topic.get(0).subtopics.subtopic.get(0))
      assert("subtopic2"===topics.topic.get(0).subtopics.subtopic.get(1))
      assert("topic2"===topics.topic.get(1).name)
      assert(2===topics.topic.get(1).subtopics.subtopic.size)
      assert("subtopic3"===topics.topic.get(1).subtopics.subtopic.get(0))
      assert("subtopic4"===topics.topic.get(1).subtopics.subtopic.get(1))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services)
      verify(percussionServices).logout()
    }
  }

  feature("makeTopicsString") {

    scenario("null topics") {
      assert("" === services.makeTopicsString(null))
    }

    scenario("empty topics") {
      assert("" === services.makeTopicsString(new Topics()))
    }

    scenario("null or empty subtopics") {

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1",null)
      topicList += new Topic("topic2",new Subtopics(List()))
      topicList += new Topic("topic3",new Subtopics(null))
      topics.topic = topicList.toList

      assert("topic1|topic2|topic3" === services.makeTopicsString(topics))
    }

    scenario("two topics with two subtopics each") {

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1",new Subtopics(List("subtopic1","subtopic2")))
      topicList += new Topic("topic2",new Subtopics(List("subtopic3","subtopic4")))
      topics.topic = topicList.toList

      assert("topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4" === services.makeTopicsString(topics))
    }
  }
}

class ArticlesCmsServices extends PercussionContentServices with LogHelper {

   var services : PercussionContentServices = this
   var servicesConnector: ServicesConnector = _
   var converterUtils : RhythmyxContentTypeFieldUtils = new RhythmyxContentTypeFieldUtils()

    def createArticle(article:Article) : String = {

      var fields = Map[String,Object]()
      fields += ("id" -> article.id)
      fields += ("link" -> article.link)
      fields += ("article_title" -> article.title)
      fields += ("body" -> article.body.replace("<![CDATA[[","").dropRight("]]".length))
      fields += ("rank" -> article.rank)
      fields += ("updated" -> article.updated)
      fields += ("topics_subtopics" -> makeTopicsString(article.topics))

      servicesConnector.configureServices(this)
      val targetFolder = servicesConnector.getTargetFolders()(0)

      services.login()
      val id = services.createItem(fields, targetFolder, "faqArticle").toString
      services.logout()
      id
    }

  def getAll() : List[Article] = {

    servicesConnector.configureServices(this)
    val targetFolder = servicesConnector.getTargetFolders()(0)

    try {
      services.login()

      val summaries : Array[PSItemSummary] = services.findFolderChildren(targetFolder)
      if (summaries.length>0) {

        val articles = new ListBuffer[Article]()

        for(summary <- summaries) {
          val contentTypeName = (summary:PSItemSummary) => {
            val contentType = summary.getContentType
            if (contentType==null) {
              logger.error("ContentType was null for PSItemSummary with id=" + summary.getId())
            } else {
              if (contentType.getName==null||contentType.getName.length==0) {
                logger.error("ContentType.name was null for PSItemSummary with id=" + summary.getId())
                null
              } else {
                contentType.getName
              }
            }
          }
          if (contentTypeName(summary)=="faqArticle") {
            val psItem = services.loadItem(summary.getId)
            val fields = psItem.getFields
            val article: Article = new Article()
            for(field <- fields) {
              val data: String = field.getPSFieldValue(0).getRawData
              if(field.getName=="id") {
                article.id = data
              } else if(field.getName=="link") {
                article.link = data
              } else if(field.getName=="body") {
                article.body = data
              } else if(field.getName=="rank") {
                article.rank = data
              } else if(field.getName=="updated") {
                article.updated = data
              } else if(field.getName=="article_title") {
                article.title = data
              } else if(field.getName=="topics_subtopics") {
                article.topics = new TopicsConverter().convertField(field)
              }
            }
            articles += article
          }
        }
        articles.toList
      } else {
        null
      }
    } finally {
      try {
        services.logout()
      } catch {
        case e: Exception => (logger.error(e.getMessage))
      }
    }
  }

    def makeTopicsString (topics:Topics) : String = {
      if(topics!=null) {
        val topicList = topics.topic
        var topicsString : String = ""
        if(topicList!=null && topicList.size()>0) {
          for(topic <- topicList) {
            topicsString = topicsString + topic.name
            if (topic.subtopics!=null) {
              val subtopics = topic.subtopics.subtopic
              if(subtopics!=null && subtopics.size()>0) {
                topicsString = topicsString + "-"
                for(subtopic <- subtopics) {
                  topicsString = topicsString + subtopic + ","
                }
                topicsString = topicsString.dropRight(1)
              }
            }
            topicsString = topicsString + "|"
          }
          topicsString = topicsString.dropRight(1)
        }
        topicsString
      } else {
        ""
      }
    }
}


