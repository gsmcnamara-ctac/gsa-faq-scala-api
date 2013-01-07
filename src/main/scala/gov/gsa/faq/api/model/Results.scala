package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name = "results", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Results(

  @XmlElement var result: java.util.List[Result]) {

  def this() {
    this(null)
  }
}
