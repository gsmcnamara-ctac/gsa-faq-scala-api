package gov.gsa.faq.api.cms

import com.ctacorp.rhythmyx.soap.FieldConverter
import gov.gsa.faq.api.model.{Subtopics, Topic, Topics}
import com.percussion.webservices.content.{PSFieldValue, PSField}
import collection.mutable.ListBuffer
import scala.collection.JavaConversions._

class TopicsConverter extends FieldConverter[Topics] {

  def convertField(field:PSField) : Topics = {
    val psFieldValue: Array[PSFieldValue] = field.getPSFieldValue
    val topicsString = psFieldValue(0).getRawData
    val topicsList = topicsString.split('|')
    if (topicsList.size>0) {
      val topicList = new ListBuffer[Topic]()
      for (topicString <- topicsList) {
        val topic = new Topic()
        if (topicString.contains("-")) {
          val topicStrings: Array[String] = topicString.split('-')
          topic.name = if(topicStrings.length>0) topicStrings(0) else topicString
          if (topicStrings.length>1) {
            val subtopicsStrings = topicStrings(1).split(',')
            val subtopicList = new ListBuffer[String]()
            for (subtopicString <- subtopicsStrings) {
              subtopicList += subtopicString
            }
            topic.subtopics = new Subtopics(subtopicList.toList)
          }
        }
        topicList += topic
      }
      new Topics(topicList.toList)
    } else {
      null
    }
  }
}
