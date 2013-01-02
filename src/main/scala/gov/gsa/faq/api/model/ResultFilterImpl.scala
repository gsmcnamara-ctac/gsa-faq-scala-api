package gov.gsa.faq.api.model

import gov.gsa.rest.api.model.ResultFilter
import scala.collection.JavaConversions._

class ResultFilterImpl extends ResultFilter {

  def getResultFilters() : java.util.List[String] = {
    val resultFilters : java.util.List[String] = new java.util.ArrayList[String]()
    resultFilters += "id"
    resultFilters += "link"
    resultFilters += "title"
    resultFilters += "body"
    resultFilters += "rank"
    resultFilters += "updated"
    resultFilters += "topic"
    resultFilters
  }
}
