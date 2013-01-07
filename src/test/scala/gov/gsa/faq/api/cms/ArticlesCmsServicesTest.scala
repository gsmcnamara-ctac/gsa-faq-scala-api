package gov.gsa.faq.api.cms

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import com.ctacorp.rhythmyx.soap.{ServicesConnector, PercussionContentServices}
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer
import org.mockito.Mockito._
import com.percussion.webservices.content._
import com.percussion.webservices.common.Reference
import gov.gsa.faq.api.Constants
import java.io.File
import gov.gsa.faq.api.model.Subtopics
import gov.gsa.faq.api.model.Topic
import gov.gsa.faq.api.model.Topics
import gov.gsa.faq.api.model.Article

@RunWith(classOf[JUnitRunner])
class ArticlesCmsServicesTest extends FeatureSpec with BeforeAndAfter {

  val services = new ArticlesCmsServices()
  var percussionServices = mock(classOf[PercussionContentServices])
  var servicesConnector = mock(classOf[ServicesConnector])
  var guidFactory = mock(classOf[GuidFactory])

  before {
    services.services = percussionServices
    services.servicesConnector = servicesConnector
    services.guidFactory = guidFactory
    reset(percussionServices, servicesConnector, guidFactory)
  }

  feature("createArticle") {

    scenario("article has all fields, two topics each with 2 subtopics") {

      var fields = Map[String, Object]()
      fields += ("id" -> "id")
      fields += ("link" -> "link")
      fields += ("article_title" -> "title")
      fields += ("body" -> "body")
      fields += ("rank" -> "rank")
      fields += ("updated" -> "updated")
      fields += ("sys_title" -> "title")
      fields += ("topics_subtopics" -> "topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")

      val article = new Article()
      article.body = "<![CDATA[body]]"
      article.id = "id"
      article.link = "link"
      article.rank = "rank"
      article.title = "title"
      article.updated = "updated"

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1", new Subtopics(List("subtopic1", "subtopic2")))
      topicList += new Topic("topic2", new Subtopics(List("subtopic3", "subtopic4")))
      topics.topic = topicList.toList

      article.topics = topics

      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolder"))
      when(percussionServices.createItem(fields, "targetFolder", "faqArticle")).thenReturn(1234)

      assert("1234" === services.createArticle(article))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices).logout()
    }
  }

  feature("configureServices") {

    scenario("properties file doesn't exist") {

      new File(Constants.SERVICES_PROPS).delete()

      val connector: ServicesConnector = new ServicesConnector()
      services.servicesConnector = connector
      services.configureServices
      assert(Array("//Sites/EnterpriseInvestments/faqArticle") === connector.getTargetFolders)
    }

    scenario("properties file exists") {

      assert(new File(Constants.SERVICES_PROPS).exists)
      val connector: ServicesConnector = new ServicesConnector()
      services.servicesConnector = connector
      services.configureServices
      assert(Array("//Sites/EnterpriseInvestments/faqArticle") === connector.getTargetFolders)
    }
  }

  feature("updateArticle") {

    scenario("item exists") {

      val item = makeItem()
      val article = makeArticle

      when(percussionServices.loadItem(2l)).thenReturn(item)
      when(guidFactory.getNewRevisionGUID(2l)).thenReturn(123)

      assert("123" === services.updateArticle(article, "2"))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices).logout()
    }

    scenario("item doesn't exist") {

      var fields = Map[String, Object]()
      fields += ("id" -> "id")
      fields += ("link" -> "link")
      fields += ("article_title" -> "title")
      fields += ("body" -> "body")
      fields += ("rank" -> "rank")
      fields += ("updated" -> "updated")
      fields += ("sys_title" -> "title")
      fields += ("topics_subtopics" -> "topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")

      val article = makeArticle

      when(percussionServices.loadItem(2l)).thenReturn(null)
      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolder"))
      when(percussionServices.createItem(fields, "targetFolder", "faqArticle")).thenReturn(1234l)

      assert("1234" === services.updateArticle(article, "2"))

      verify(percussionServices, times(2)).login()
      verify(servicesConnector, times(2)).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices, times(2)).logout()
    }

    scenario("loadItem throws") {

      var fields = Map[String, Object]()
      fields += ("id" -> "id")
      fields += ("link" -> "link")
      fields += ("article_title" -> "title")
      fields += ("body" -> "body")
      fields += ("rank" -> "rank")
      fields += ("updated" -> "updated")
      fields += ("sys_title" -> "title")
      fields += ("topics_subtopics" -> "topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")

      val article = makeArticle

      when(percussionServices.loadItem(2l)).thenThrow(new Exception("meh"))
      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolder"))
      when(percussionServices.createItem(fields, "targetFolder", "faqArticle")).thenReturn(1234l)

      assert("1234" === services.updateArticle(article, "2"))

      verify(percussionServices, times(2)).login()
      verify(servicesConnector, times(2)).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices, times(2)).logout()
    }
  }

  def makeTopicsField: PSField = {
    val topicsField = new PSField()
    topicsField.setName("topics_subtopics")
    val topicsValue: PSFieldValue = new PSFieldValue()
    topicsValue.setRawData("topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")
    topicsField.setPSFieldValue(Array(topicsValue))
    topicsField
  }

  def makeItem(): PSItem = {

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

    val topicsField: PSField = makeTopicsField

    val item: PSItem = mock(classOf[PSItem])
    when(item.getFields).thenReturn(Array[PSField](idField, linkField, titleField, bodyField, rankField, updatedField, topicsField))
    when(item.getId).thenReturn(2l)
    item
  }

  def makeArticle: Article = {
    val article = new Article()
    article.body = "<![CDATA[body]]"
    article.id = "id"
    article.link = "link"
    article.rank = "rank"
    article.title = "title"
    article.updated = "updated"
    article.topics = new TopicsConverter().convertField(makeTopicsField)
    article
  }

  feature("getArticle") {

    scenario("load 1 item from the CMS and return a single Articles") {

      val item = makeItem
      when(percussionServices.loadItem(2l)).thenReturn(item)

      val article = services.getArticle(2l)
      assert(article.body === "<![CDATA[body]]")
      assert(article.id === "id")
      assert(article.link === "link")
      assert(article.rank === "rank")
      assert(article.title === "title")
      assert(article.updated === "updated")
      assert(article.topics === new TopicsConverter().convertField(makeTopicsField))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices).logout()
    }
  }

  feature("getAllArticles") {

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

      when(item.getFields).thenReturn(Array[PSField](idField, linkField, titleField, bodyField, rankField, updatedField, topicsField))

      when(percussionServices.loadItem(1234)).thenReturn(item)

      val articles = services.getAllArticles()
      assert(1 === articles.size)
      assert("id" === articles(0).id)
      assert("link" === articles(0).link)
      assert("body" === articles(0).body)
      assert("rank" === articles(0).rank)
      assert("title" === articles(0).title)
      assert("updated" === articles(0).updated)

      val topics = articles(0).topics
      assert(2 === topics.topic.size)
      assert("topic1" === topics.topic.get(0).name)
      assert(2 === topics.topic.get(0).subtopics.subtopic.size)
      assert("subtopic1" === topics.topic.get(0).subtopics.subtopic.get(0))
      assert("subtopic2" === topics.topic.get(0).subtopics.subtopic.get(1))
      assert("topic2" === topics.topic.get(1).name)
      assert(2 === topics.topic.get(1).subtopics.subtopic.size)
      assert("subtopic3" === topics.topic.get(1).subtopics.subtopic.get(0))
      assert("subtopic4" === topics.topic.get(1).subtopics.subtopic.get(1))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
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
      topicList += new Topic("topic1", null)
      topicList += new Topic("topic2", new Subtopics(List()))
      topicList += new Topic("topic3", new Subtopics(null))
      topics.topic = topicList.toList

      assert("topic1|topic2|topic3" === services.makeTopicsString(topics))
    }

    scenario("two topics with two subtopics each") {

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1", new Subtopics(List("subtopic1", "subtopic2")))
      topicList += new Topic("topic2", new Subtopics(List("subtopic3", "subtopic4")))
      topics.topic = topicList.toList

      assert("topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4" === services.makeTopicsString(topics))
    }
  }
}