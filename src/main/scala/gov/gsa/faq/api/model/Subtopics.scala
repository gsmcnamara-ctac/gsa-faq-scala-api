package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import collection.mutable.ArrayBuffer

@XmlRootElement(name = "subtopics", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Subtopics (

  @XmlElement var subtopic: Seq[String]
) {
}
