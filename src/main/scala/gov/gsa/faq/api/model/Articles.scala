package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name="articles",namespace="")
@XmlAccessorType(XmlAccessType.FIELD)
case class Articles(

  @XmlElement var article: java.util.List[Article]) {

  def this() {
    this(null)
  }
}
