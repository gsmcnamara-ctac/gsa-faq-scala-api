package gov.gsa.faq.api.cms

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import com.ctacorp.rhythmyx.soap.{FieldConverter, ServicesConnector, PercussionContentServices}
import gov.gsa.faq.api.model.{Subtopics, Topic, Topics, Article}
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import org.mockito.Mockito.verify
import com.percussion.webservices.content.{PSField, PSItem, PSItemSummary}
import gov.gsa.faq.api.LogHelper

@RunWith(classOf[JUnitRunner])
class ArticlesCmsServicesTest extends FeatureSpec with BeforeAndAfter {

  val services = new ArticlesCmsServices()
  val percussionServices = mock(classOf[PercussionContentServices])
  val servicesConnector = mock(classOf[ServicesConnector])

  before {
    services.services = percussionServices
    services.servicesConnector = servicesConnector
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

      assert("1234"==services.createArticle(article))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services)
      verify(percussionServices).logout()
    }
  }

  feature("makeTopicsString") {

    scenario("null topics") {
      assert("" == services.makeTopicsString(null), services.makeTopicsString(null))
    }

    scenario("empty topics") {
      assert("" == services.makeTopicsString(new Topics()), services.makeTopicsString(new Topics()))
    }

    scenario("null or empty subtopics") {

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1",null)
      topicList += new Topic("topic2",new Subtopics(List()))
      topicList += new Topic("topic3",new Subtopics(null))
      topics.topic = topicList.toList

      assert("topic1|topic2|topic3" == services.makeTopicsString(topics), services.makeTopicsString(topics))
    }

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

class ArticlesCmsServices extends PercussionContentServices with LogHelper {

   var services : PercussionContentServices = this
   var servicesConnector: ServicesConnector = _

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

    services.login()

    val summaries : Array[PSItemSummary] = services.findFolderChildren(targetFolder)
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
      if (contentTypeName=="faqArticle") {
        var psItem = services.loadItem(summary.getId)

      }
    }

    services.logout()

//    List<MobApplication> applications = new ArrayList<MobApplication>();
//
//    for (String targetFolder : targetFolders) {
//      PSItemSummary[] summaries = services.findFolderChildren(targetFolder);
//      for (PSItemSummary summary : summaries) {
//        Reference contentType = summary.getContentType();
//        if (contentType == null) {
//          System.err.println("ContentType was null for PSItemSummary with id=" + summary.getId());
//          continue;
//        }
//        String name = contentType.getName();
//        if (StringUtils.isEmpty(name)) {
//          System.err.println("ContentType.name was null for PSItemSummary with id=" + summary.getId());
//          continue;
//        }
//        if (name.equals("mobApplication")) {
//          MobApplication mobApplication = getMobApplication(summary.getId());
//          applications.add(mobApplication);
//        }
//      }
//    }
//
//    return applications;

    null
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


