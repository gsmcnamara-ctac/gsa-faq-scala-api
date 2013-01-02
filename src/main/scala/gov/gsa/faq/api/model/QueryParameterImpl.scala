package gov.gsa.faq.api.model

import gov.gsa.rest.api.model.QueryParameter
import scala.collection.JavaConversions._

class QueryParameterImpl extends QueryParameter {

  def getQueryParams(): java.util.List[String] = {
    val queryParams : java.util.List[String] = new java.util.ArrayList[String]()
    queryParams += "id"
    queryParams += "title"
    queryParams += "body"
    queryParams += "rank"
    queryParams += "topic"
    queryParams += "subtopic"
    queryParams
  }
}
