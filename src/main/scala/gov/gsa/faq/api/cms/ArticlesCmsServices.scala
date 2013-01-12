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

    configureServices
    val targetFolder = {
      if (article.language == "ES") {
        servicesConnector.getTargetFolders()(1)
      } else {
        servicesConnector.getTargetFolders()(0)
      }
    }

    services.login()
    val id = services.createItem(fields, targetFolder, "faqTest").toString
    services.logout()
    id
  }

  def mapArticleToFields(article: Article): Map[String, Object] = {
    var fields = Map[String, Object]()
    fields += ("id" -> article.id)
    fields += ("link" -> article.link)
    fields += ("article_title" -> article.title)
    val body = article.body.replace("<![CDATA[", "").dropRight("]]".length)
    fields += ("body" -> body)
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

    def getLanguage(psItem: PSItem): String = {
      val language = {
        if (psItem != null) {
          val targetFolder = psItem.getFolders()(0).getPath
          if (targetFolder == Constants.XML_PATH) {
            "EN"
          } else if (targetFolder == Constants.XML_PATH_ES) {
            "ES"
          } else {
            null
          }
        } else {
          null
        }
      }
      language
    }

    try {
      services.login()

      val psItem = services.loadItem(id)
      val language: String = getLanguage(psItem)

      if (language != null) {
        val article: Article = new Article()
        article.language = language
        val fields = psItem.getFields
        for (field <- fields) {
          val value = field.getPSFieldValue
          if (value != null && value.length > 0) {
            val data: String = value(0).getRawData
            if (field.getName == "id") {
              article.id = data
            } else if (field.getName == "link") {
              article.link = data
            } else if (field.getName == "body") {
              article.body = "<![CDATA[" + data + "]]"
            } else if (field.getName == "language") {
              article.language = data
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

    val articles = new ListBuffer[Article]()

    configureServices
    val targetFolders = servicesConnector.getTargetFolders()
    for (targetFolder <- targetFolders) {

      val language = {
        if (targetFolder == Constants.XML_PATH) {
          "EN"
        } else {
          "ES"
        }
      }

      try {
        services.login()

        val summaries: Array[PSItemSummary] = services.findFolderChildren(targetFolder)
        if (summaries.length > 0) {

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
              val article: Article = new Article()
              article.language = language
              val psItem = services.loadItem(summary.getId)
              val fields = psItem.getFields
              for (field <- fields) {
                val value = field.getPSFieldValue
                if (value != null) {
                  val data: String = value(0).getRawData
                  if (field.getName == "id") {
                    article.id = data
                  } else if (field.getName == "link") {
                    article.link = data
                  } else if (field.getName == "body") {
                    article.body = data
                  } else if (field.getName == "language") {
                    article.language = data
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

    articles.toList
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
