package gov.gsa.faq.api.model

import scala.collection.JavaConversions._

class ArticlesConverter {

  def toArticles(xml: String): Seq[Article] = {
    val rootElement = scala.xml.XML.loadFile(xml);

    val articlesNode = (rootElement \ "articles").map { articles =>
      val articleList = (articles \ "article").map { article =>
          val id = (article \ "id").text
          val link = (article \ "link").text
          val title = (article \ "title").text
          val body = (article \ "body").text
          val rank = (article \ "rank").text
          val updated = (article \ "updated").text
          val topicsNode = (article \ "topics").map { topics =>
            val topicList = (topics \ "topic").map { topic =>
              val name = (topic \ "name").text
              val subtopicsNode = (topic \ "subtopics").map { subtopics =>
                val subtopicsList = (subtopics \ "subtopic").map { subtopic =>
                  subtopic.text
                }
                Subtopics(subtopicsList.toList)
              }
              if(subtopicsNode.seq!=null && subtopicsNode.seq.size > 0) {
                Topic(name,subtopicsNode.seq(0))
              } else {
                Topic(name,null)
              }
            }
            Topics(topicList.toList)
          }
          Article(id, link, title, body, rank, updated, topicsNode.toList(0))
      }
      Articles(articleList.toList)
    }

    return articlesNode.seq(0).article
  }
}
