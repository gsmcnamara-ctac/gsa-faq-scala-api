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
import org.scalatest.matchers.ShouldMatchers._

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

    def getFields: Map[String, Object] = {
      var fields = Map[String, Object]()
      fields += ("faq_id" -> "id")
      fields += ("faq_src_link" -> "link")
      fields += ("displaytitle" -> "title")
      fields += ("faq_src_title" -> "title")
      fields += ("description" -> "body")
      fields += ("text" -> "body")
      fields += ("faq_rank" -> "rank")
      fields += ("faq_updated" -> "updated")
      fields += ("faq_topic_subtopic" -> "topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")
      fields += ("sys_title" -> "title")
      fields += ("top_link" -> "No")
      fields += ("title_style" -> "Hidden")
      fields += ("pagetitle" -> "title")
      fields
    }

    def getArticle: Article = {
      val article = new Article()
      article.body = "<![CDATA[body]]"
      article.id = "id"
      article.link = "link"
      article.rank = "rank"
      article.title = "title"
      article.updated = "updated"
      article
    }

    scenario("English article has all fields, two topics each with 2 subtopics") {

      val fields = getFields
      val article = getArticle
      article.language = "EN"

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1", new Subtopics(List("subtopic1", "subtopic2")))
      topicList += new Topic("topic2", new Subtopics(List("subtopic3", "subtopic4")))
      topics.topic = topicList.toList

      article.topics = topics

      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolderEnglish", "targetFolderSpanish"))
      when(percussionServices.createItem(fields, "targetFolderEnglish", "gsaArticle")).thenReturn(1234)

      assert("1234" === services.createArticle(article))

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices).logout()
    }

    scenario("Spanish article has all fields, two topics each with 2 subtopics") {

      val fields = getFields
      val article = getArticle
      article.language = "ES"

      val topics = new Topics()
      var topicList = new ListBuffer[Topic]
      topicList += new Topic("topic1", new Subtopics(List("subtopic1", "subtopic2")))
      topicList += new Topic("topic2", new Subtopics(List("subtopic3", "subtopic4")))
      topics.topic = topicList.toList

      article.topics = topics

      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolderEnglish", "targetFolderSpanish"))
      when(percussionServices.createItem(fields, "targetFolderSpanish", "gsaArticle")).thenReturn(1234)

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
      assert(Array("//Sites/EnterpriseInvestments/faqArticle", "//Sites/EnterpriseInvestments/faqArticle_ES") === connector.getTargetFolders)
    }

    scenario("properties file exists") {

      assert(new File(Constants.SERVICES_PROPS).exists)
      val connector: ServicesConnector = new ServicesConnector()
      services.servicesConnector = connector
      services.configureServices
      assert(Array("//Sites/EnterpriseInvestments/faqArticle", "//Sites/EnterpriseInvestments/faqArticle_ES") === connector.getTargetFolders)
    }
  }

  feature("updateArticle") {

    def makeFields: Map[String, Object] = {
      var fields = Map[String, Object]()
      fields += ("faq_id" -> "id")
      fields += ("faq_src_link" -> "link")
      fields += ("displaytitle" -> "title")
      fields += ("faq_src_title" -> "title")
      fields += ("description" -> "body")
      fields += ("text" -> "body")
      fields += ("faq_rank" -> "rank")
      fields += ("faq_updated" -> "updated")
      fields += ("faq_topic_subtopic" -> "topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")
      fields += ("sys_title" -> "title")
      fields += ("top_link" -> "No")
      fields += ("title_style" -> "Hidden")
      fields += ("pagetitle" -> "title")
      fields
    }

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

      val fields = makeFields
      val article = makeArticle

      when(percussionServices.loadItem(2l)).thenReturn(null)
      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolderEnglish", "targetFolderSpanish"))
      when(percussionServices.createItem(fields, "targetFolderEnglish", "gsaArticle")).thenReturn(1234l)

      assert("1234" === services.updateArticle(article, "2"))

      verify(percussionServices, times(2)).login()
      verify(servicesConnector, times(2)).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices, times(2)).logout()
    }

    scenario("loadItem throws") {

      val fields = makeFields
      val article = makeArticle

      when(percussionServices.loadItem(2l)).thenThrow(new Exception("meh"))
      when(servicesConnector.getTargetFolders).thenReturn(Array("targetFolderEnglish", "targetFolderSpanish"))
      when(percussionServices.createItem(fields, "targetFolderEnglish", "gsaArticle")).thenReturn(1234l)

      assert("1234" === services.updateArticle(article, "2"))

      verify(percussionServices, times(2)).login()
      verify(servicesConnector, times(2)).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices, times(2)).logout()
    }
  }

  def makeTopicsField: PSField = {
    val topicsField = new PSField()
    topicsField.setName("faq_topic_subtopic")
    val topicsValue: PSFieldValue = new PSFieldValue()
    topicsValue.setRawData("topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")
    topicsField.setPSFieldValue(Array(topicsValue))
    topicsField
  }

  def makeItem(): PSItem = {

    val idField = new PSField()
    idField.setName("faq_id")
    val idValue = new PSFieldValue()
    idValue.setRawData("id")
    idField.setPSFieldValue(Array[PSFieldValue](idValue))

    val linkField = new PSField()
    linkField.setName("faq_src_link")
    val linkValue = new PSFieldValue()
    linkValue.setRawData("link")
    linkField.setPSFieldValue(Array[PSFieldValue](linkValue))

    val titleField = new PSField()
    titleField.setName("displaytitle")
    val titleValue = new PSFieldValue()
    titleValue.setRawData("title")
    titleField.setPSFieldValue(Array[PSFieldValue](titleValue))

    val titleField2 = new PSField()
    titleField2.setName("faq_src_title")
    val titleValue2 = new PSFieldValue()
    titleValue2.setRawData("title")
    titleField2.setPSFieldValue(Array[PSFieldValue](titleValue2))

    val bodyField = new PSField()
    bodyField.setName("text")
    val bodyValue = new PSFieldValue()
    bodyValue.setRawData("body")
    bodyField.setPSFieldValue(Array[PSFieldValue](bodyValue))

    val bodyField2 = new PSField()
    bodyField2.setName("description")
    val bodyValue2 = new PSFieldValue()
    bodyValue2.setRawData("body")
    bodyField2.setPSFieldValue(Array[PSFieldValue](bodyValue2))

    val rankField = new PSField()
    rankField.setName("faq_rank")
    val rankValue = new PSFieldValue()
    rankValue.setRawData("rank")
    rankField.setPSFieldValue(Array[PSFieldValue](rankValue))

    val updatedField = new PSField()
    updatedField.setName("faq_updated")
    val updatedValue = new PSFieldValue()
    updatedValue.setRawData("updated")
    updatedField.setPSFieldValue(Array[PSFieldValue](updatedValue))

    val topicsField: PSField = makeTopicsField

    val item: PSItem = mock(classOf[PSItem])
    when(item.getFields).thenReturn(Array[PSField](idField, linkField, titleField, titleField2, bodyField, bodyField2, rankField, updatedField, topicsField))
    when(item.getId).thenReturn(2l)
    item
  }

  def makeArticle: Article = {
    val article = new Article()
    article.body = "<![CDATA[body]]"
    article.id = "id"
    article.link = "link"
    article.language = "EN"
    article.rank = "rank"
    article.title = "title"
    article.updated = "updated"
    article.topics = new TopicsConverter().convertField(makeTopicsField)
    article
  }

  feature("getArticle") {

    scenario("load 1 item from the CMS and return a single Article") {

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

    scenario("load 1 item from the CMS and return a single Article with no topics") {

      val idField = new PSField()
      idField.setName("faq_id")
      val idValue = new PSFieldValue()
      idValue.setRawData("id")
      idField.setPSFieldValue(Array[PSFieldValue](idValue))

      val item: PSItem = mock(classOf[PSItem])

      when(item.getFields).thenReturn(Array[PSField](idField))
      when(item.getId).thenReturn(2l)

      when(percussionServices.loadItem(2l)).thenReturn(item)

      val article = services.getArticle(2l)
      assert(article.id === "id")
      article.topics should be(null)

      verify(percussionServices).login()
      verify(servicesConnector).configureServices(services, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
      verify(percussionServices).logout()
    }

    scenario("load 1 item from the CMS with a null field value") {

      val idField = new PSField()
      idField.setName("faq_id")
      val idValue = new PSFieldValue()
      idValue.setRawData("id")
      idField.setPSFieldValue(Array[PSFieldValue](idValue))

      val linkField = new PSField()
      linkField.setName("link")
      val linkValue = new PSFieldValue()
      linkValue.setRawData("link")

      val item: PSItem = mock(classOf[PSItem])

      when(item.getFields).thenReturn(Array[PSField](idField))
      when(item.getId).thenReturn(2l)

      when(percussionServices.loadItem(2l)).thenReturn(item)

      val article = services.getArticle(2l)
      assert(article.id === "id")
      article.topics should be(null)

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