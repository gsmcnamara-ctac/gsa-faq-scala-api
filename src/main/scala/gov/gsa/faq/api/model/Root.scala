package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name="root",namespace="")
@XmlAccessorType(XmlAccessType.FIELD)
class Root {

  @XmlElement var articles: Articles = _
}
