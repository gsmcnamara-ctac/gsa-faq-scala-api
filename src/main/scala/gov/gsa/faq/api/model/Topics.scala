package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name = "topics", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Topics(

  @XmlElement var topic: java.util.List[Topic]){

  def this() {
    this(null)
  }
}
