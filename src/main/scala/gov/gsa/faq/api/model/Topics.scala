package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import collection.mutable.ArrayBuffer

@XmlRootElement(name = "topics", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Topics(

  @XmlElement var topic: Seq[Topic]){
}
