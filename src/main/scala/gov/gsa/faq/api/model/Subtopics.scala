package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name = "subtopics", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Subtopics (

  @XmlElement var subtopic: java.util.List[String]) {

  def this() {
    this(null)
  }
}
