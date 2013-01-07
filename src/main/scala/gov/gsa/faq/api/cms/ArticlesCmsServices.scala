package gov.gsa.faq.api.cms

import com.ctacorp.rhythmyx.soap.{ServicesConnector, PercussionContentServices}
import gov.gsa.faq.api.model.{Topics, Article}
import com.percussion.webservices.content.{PSField, PSItem, PSItemSummary}
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

    configureServices
    val targetFolder = servicesConnector.getTargetFolders()(0)

    services.login()
    val id = services.createItem(fields, targetFolder, "faqArticle").toString
    services.logout()
    id
  }

  def mapArticleToFields(article: Article): Map[String, Object] = {
    var fields = Map[String, Object]()
    fields += ("id" -> article.id)
    fields += ("link" -> article.link)
    fields += ("article_title" -> article.title)
    fields += ("body" -> article.body.replace("<![CDATA[", "").dropRight("]]".length))
    fields += ("rank" -> article.rank)
    fields += ("updated" -> article.updated)
    fields += ("topics_subtopics" -> makeTopicsString(article.topics))
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

  def updateArticle(article: Article, id: String): Boolean = {

    configureServices

    try {

      services.login()

      val item: PSItem = services.loadItem(id.toLong)
      val fields = item.getFields

      var updated = false
      if (isDifferent(fields, article)) {
        updated = services.updateItem(item, mapArticleToFields(article), guidFactory.getNewRevisionGUID(id.toLong))
      } else {
        updated = true
      }
      updated
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        false
      }
    } finally {
      try {
        services.logout()
      } catch {
        case e: Exception => (logger.error(e.getMessage))
      }
    }
  }

  def isDifferent(fields: Array[PSField], article: Article): Boolean = {
    var isDifferent = false
    for (field <- fields) {
      val data: String = field.getPSFieldValue(0).getRawData
      if (field.getName == "link") {
        if (article.link != data) {
          isDifferent = true
        }
      } else if (field.getName == "body") {
        if (article.body != "<![CDATA[" + data + "]]") {
          isDifferent = true
        }
      } else if (field.getName == "rank") {
        if (article.rank != data) {
          isDifferent = true
        }
      } else if (field.getName == "updated") {
        if (article.updated != data) {
          isDifferent = true
        }
      } else if (field.getName == "article_title") {
        if (article.title != data) {
          isDifferent = true
        }
      } else if (field.getName == "topics_subtopics") {
        if (makeTopicsString(article.topics) != data) {
          isDifferent = true
        }
      }
    }
    isDifferent
  }

  def getArticle(id: Long): Article = {
    configureServices

    try {
      services.login()
      val psItem = services.loadItem(id)
      if (psItem != null) {
        val fields = psItem.getFields
        val article: Article = new Article()
        for (field <- fields) {
          val data: String = field.getPSFieldValue(0).getRawData
          if (field.getName == "id") {
            article.id = data
          } else if (field.getName == "link") {
            article.link = data
          } else if (field.getName == "body") {
            article.body = "<![CDATA[" + data + "]]"
          } else if (field.getName == "rank") {
            article.rank = data
          } else if (field.getName == "updated") {
            article.updated = data
          } else if (field.getName == "article_title") {
            article.title = data
          } else if (field.getName == "topics_subtopics") {
            article.topics = new TopicsConverter().convertField(field)
          }
        }
        article
      } else {
        null
      }
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

  def getAllArticles(): List[Article] = {

    configureServices
    val targetFolder = servicesConnector.getTargetFolders()(0)

    try {
      services.login()

      val summaries: Array[PSItemSummary] = services.findFolderChildren(targetFolder)
      if (summaries.length > 0) {

        val articles = new ListBuffer[Article]()

        for (summary <- summaries) {
          val contentTypeName = (summary: PSItemSummary) => {
            val contentType = summary.getContentType
            if (contentType == null) {
              logger.error("ContentType was null for PSItemSummary with id=" + summary.getId())
            } else {
              if (contentType.getName == null || contentType.getName.length == 0) {
                logger.error("ContentType.name was null for PSItemSummary with id=" + summary.getId())
                null
              } else {
                contentType.getName
              }
            }
          }
          if (contentTypeName(summary) == "faqArticle") {
            val psItem = services.loadItem(summary.getId)
            val fields = psItem.getFields
            val article: Article = new Article()
            for (field <- fields) {
              val data: String = field.getPSFieldValue(0).getRawData
              if (field.getName == "id") {
                article.id = data
              } else if (field.getName == "link") {
                article.link = data
              } else if (field.getName == "body") {
                article.body = data
              } else if (field.getName == "rank") {
                article.rank = data
              } else if (field.getName == "updated") {
                article.updated = data
              } else if (field.getName == "article_title") {
                article.title = data
              } else if (field.getName == "topics_subtopics") {
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
