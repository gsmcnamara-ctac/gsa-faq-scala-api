package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import collection.mutable.ArrayBuffer

@XmlRootElement(name="articles",namespace="")
@XmlAccessorType(XmlAccessType.FIELD)
case class Articles(

  @XmlElement var article: Seq[Article]
) {
}
