package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import collection.mutable.ArrayBuffer

@XmlRootElement(name="articles",namespace="")
@XmlAccessorType(XmlAccessType.FIELD)
class Articles {

  @XmlElement var article: ArrayBuffer[Article] = _
}