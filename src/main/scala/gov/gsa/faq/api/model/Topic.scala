package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import collection.mutable.ArrayBuffer

@XmlRootElement(name = "topic", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Topic(

  @XmlElement var name: String,
  @XmlElement var subtopics: Subtopics) {

  def this() {
    this(null,null)
  }
}
