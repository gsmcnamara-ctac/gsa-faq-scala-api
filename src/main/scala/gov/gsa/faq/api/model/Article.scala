package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import collection.mutable.ArrayBuffer

@XmlRootElement(name = "article", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Article(
  @XmlElement val id: String,
  @XmlElement val link: String,
  @XmlElement val title: String,
  @XmlElement val body: String,
  @XmlElement val rank: Double,
  @XmlElement val updated: String,
  @XmlElement val topics : Topics) {
}
