package gov.gsa.faq.api.model

import javax.xml.bind.annotation.{XmlElement, XmlAccessType, XmlAccessorType, XmlRootElement}
import com.ctacorp.rhythmyx.soap.RhythmyxContentTypeField
import scala.reflect.runtime.universe._

@XmlRootElement(name = "article", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
case class Article(

  @XmlElement @RhythmyxContentTypeField(name="id") var id: String,
  @XmlElement @RhythmyxContentTypeField(name="link") var link: String,
  @XmlElement @RhythmyxContentTypeField(name="article_title") var title: String,
  @XmlElement @RhythmyxContentTypeField(name="body") var body: String,
  @XmlElement @RhythmyxContentTypeField(name="rank") var rank: String,
  @XmlElement @RhythmyxContentTypeField(name="updated") var updated: String,
  @XmlElement @RhythmyxContentTypeField(name="topics_subtopics",converterClass="gov.gsa.faq.api.model.TopicsConverter") var topics : Topics) {

  def this() {
    this(null,null,null,null,null,null,null)
  }
}
