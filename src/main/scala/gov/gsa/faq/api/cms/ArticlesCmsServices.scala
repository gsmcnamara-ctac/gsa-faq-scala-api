package gov.gsa.faq.api.cms

import com.ctacorp.rhythmyx.soap.{ServicesConnector, PercussionContentServices}
import gov.gsa.faq.api.model.{Topics, Article}
import com.percussion.webservices.content.{PSItem, PSItemSummary}
import gov.gsa.faq.api.{Constants, LogHelper}
import collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import java.io.File
import org.apache.commons.io.FileUtils
import org.springframework.core.io.ClassPathResource

class ArticlesCmsServices extends PercussionContentServices with LogHelper {

  var services: PercussionContentServices = this
  var servicesConnector: ServicesConnector = new ServicesConnector()
  var guidFactory = new GuidFactory()

  def createArticle(article: Article): String = {

    var fields = mapArticleToFields(article)
    fields += ("sys_title" -> article.title)
    fields += ("top_link" -> "No")
    fields += ("title_style" -> "Hidden")
    fields += ("pagetitle" -> article.title)

    configureServices
    val targetFolder = {
      if (article.language == "ES") {
        servicesConnector.getTargetFolders()(1)
      } else {
        servicesConnector.getTargetFolders()(0)
      }
    }

    services.login()
    val id = services.createItem(fields, targetFolder, "gsaArticle").toString
    services.logout()
    id
  }

  def mapArticleToFields(article: Article): Map[String, Object] = {
    var fields = Map[String, Object]()
    fields += ("faq_id" -> article.id)
    fields += ("faq_src_link" -> article.link)
    fields += ("displaytitle" -> article.title)
    fields += ("faq_src_title" -> article.title)
    val body = article.body.replace("<![CDATA[", "").dropRight("]]".length)
    fields += ("text" -> body)
    fields += ("description" -> body)
    fields += ("faq_rank" -> article.rank)
    fields += ("faq_updated" -> article.updated)
    fields += ("faq_topic_subtopic" -> makeTopicsString(article.topics))
    fields
  }

  def configureServices {
    val file: File = new File(Constants.SERVICES_PROPS)
    if (!file.exists()) {
      val is = new ClassPathResource("services.properties").getInputStream
      FileUtils.copyInputStreamToFile(is, file)
    }
    servicesConnector.configureServices(this, Constants.DATA_DIR, Constants.SERVICES_PROPS_NAME)
  }

  def updateArticle(article: Article, id: String): String = {

    configureServices

    var item: PSItem = null
    try {
      services.login()
      item = services.loadItem(id.toLong)
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
      }
    }
    if (item == null) {
      services.logout()
      createArticle(article).toString
    } else {
      try {
        val guid = guidFactory.getNewRevisionGUID(item.getId)
        services.updateItem(item, mapArticleToFields(article), guid)
        guid.toString
      } catch {
        case e: Exception => {
          logger.error(e.getMessage)
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
  }

  def getArticle(id: Long): Article = {
    configureServices

    try {
      services.login()

      val psItem = services.loadItem(id)

      val article: Article = new Article()
      val fields = psItem.getFields
      for (field <- fields) {
        val value = field.getPSFieldValue
        if (value != null && value.length > 0) {
          val data: String = value(0).getRawData
          if (field.getName == "faq_id") {
            article.id = data
          } else if (field.getName == "faq_src_link") {
            article.link = data
          } else if (field.getName == "text") {
            article.body = "<![CDATA[" + data + "]]"
          } else if (field.getName == "faq_rank") {
            article.rank = data
          } else if (field.getName == "faq_updated") {
            article.updated = data
          } else if (field.getName == "faq_src_title") {
            article.title = data
          } else if (field.getName == "faq_topic_subtopic") {
            article.topics = new TopicsConverter().convertField(field)
          }
        }
      }
      article
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
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

  def makeTopicsString(topics: Topics): String = {
    if (topics != null) {
      val topicList = topics.topic
      var topicsString: String = ""
      if (topicList != null && topicList.size() > 0) {
        for (topic <- topicList) {
          topicsString = topicsString + topic.name
          if (topic.subtopics != null) {
            val subtopics = topic.subtopics.subtopic
            if (subtopics != null && subtopics.size() > 0) {
              topicsString = topicsString + "-"
              for (subtopic <- subtopics) {
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
