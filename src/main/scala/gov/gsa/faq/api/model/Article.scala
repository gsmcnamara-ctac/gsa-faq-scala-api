package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name = "article", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Article(

  @XmlElement var id: String,
  @XmlElement var link: String,
  @XmlElement var title: String,
  @XmlElement var body: String,
  @XmlElement var rank: String,
  @XmlElement var updated: String,
  @XmlElement var topics : Topics) {

  def this() {
    this(null,null,null,null,null,null,null)
  }
}
