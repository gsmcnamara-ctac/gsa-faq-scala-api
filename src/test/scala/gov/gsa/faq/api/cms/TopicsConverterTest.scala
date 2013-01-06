package gov.gsa.faq.api.cms

import org.scalatest.{FlatSpec, BeforeAndAfter, FeatureSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.percussion.webservices.content.{PSFieldValue, PSField}
import gov.gsa.faq.api.model.Topics

@RunWith(classOf[JUnitRunner])
class TopicsConverterTest extends FlatSpec {

  behavior of "convertField"

  it should "convert 'topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4' to a Topics object" in {

    val field: PSField = new PSField()
    val value: PSFieldValue = new PSFieldValue()
    value.setRawData("topic1-subtopic1,subtopic2|topic2-subtopic3,subtopic4")
    field.setPSFieldValue(Array(value))
    val topics = new TopicsConverter().convertField(field)

    assert(2===topics.topic.size())
    assert("topic1"===topics.topic.get(0).name)
    assert(2===topics.topic.get(0).subtopics.subtopic.size())
    assert("subtopic1"===topics.topic.get(0).subtopics.subtopic.get(0))
    assert("subtopic2"===topics.topic.get(0).subtopics.subtopic.get(1))
    assert("topic2"===topics.topic.get(1).name)
    assert(2===topics.topic.get(1).subtopics.subtopic.size())
    assert("subtopic3"===topics.topic.get(1).subtopics.subtopic.get(0))
    assert("subtopic4"===topics.topic.get(1).subtopics.subtopic.get(1))
  }
}
