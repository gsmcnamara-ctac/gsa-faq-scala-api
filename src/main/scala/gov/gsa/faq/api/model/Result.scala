package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}

@XmlRootElement(name = "result", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
class Result {

  @XmlElement var id: String = _
  @XmlElement var cmsId: String = _
  @XmlElement var operation: String = _
  @XmlElement var result: String = _
}
