package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name="article",namespace="")
@XmlAccessorType(XmlAccessType.FIELD)
class Article {

  @XmlElement var id: String = _
  @XmlElement var link: String = _
  @XmlElement var title: String = _
  @XmlElement var body: String = _
  @XmlElement var rank: Double = _
  @XmlElement var updated: String = _
  @XmlElement var topics: Topics = _
}
