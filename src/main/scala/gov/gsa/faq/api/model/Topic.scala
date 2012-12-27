package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import collection.mutable.ArrayBuffer

@XmlRootElement(name = "topic", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
class Topic {

  @XmlElement var name: String = _
  @XmlElement var subtopics: Subtopics = _
}
