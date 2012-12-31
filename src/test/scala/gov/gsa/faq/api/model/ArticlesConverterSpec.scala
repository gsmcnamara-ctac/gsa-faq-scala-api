package gov.gsa.faq.api.model

import org.scalatest.FreeSpec
import gov.gsa.faq.api.Constants
import collection.mutable.ArrayBuffer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import collection.immutable.Range.Double

@RunWith(classOf[JUnitRunner])
class ArticlesConverterSpec extends FreeSpec {

  "ArticlesConverter" - {

    "toArticles should marhsall XML as Articles" in {
      val articlesConverter = new ArticlesConverter()
      val articles: Seq[Article] = articlesConverter.toArticles(Constants.XML_PATH)
      articles.foreach(println)
    }
  }
}

class ArticlesConverter {

  def toArticles(xml: String): Seq[Article] = {
    val rootElement = scala.xml.XML.loadFile(xml);

    val articlesNode = (rootElement \ "articles").map { articles =>
      val articleList = (articles \ "article").map { article =>
          val id = (article \ "id").text
          val link = (article \ "link").text
          val title = (article \ "title").text
          val body = (article \ "body").text
          val rank = (article \ "rank").text.toDouble
          val updated = (article \ "updated").text
          val topicsNode = (article \ "topics").map { topics =>
            val topicList = (topics \ "topic").map { topic =>
              val name = (topic \ "name").text
              val subtopicsNode = (topic \ "subtopics").map { subtopics =>
                val subtopicsList = (subtopics \ "subtopic").map { subtopic =>
                  (subtopic \ "subtopic").text
                }
                Subtopics(subtopicsList.seq)
              }
              Topic(name,subtopicsNode.seq(0))
            }
            Topics(topicList.seq)
          }
          Article(id, link, title, body, rank, updated, topicsNode.seq(0))
      }
      Articles(articleList.seq)
    }

    return articlesNode.seq(0).article
  }
}

//@XmlElement val id: String,
//@XmlElement val link: String,
//@XmlElement val title: String,
//@XmlElement val body: String,
//@XmlElement val rank: Double,
//@XmlElement val updated: String,
//@XmlElement val topics : Topics) {