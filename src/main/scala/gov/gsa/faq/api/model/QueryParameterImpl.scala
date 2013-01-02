package gov.gsa.faq.api.model

import gov.gsa.rest.api.model.QueryParameter
import scala.collection.immutable.List;

class QueryParameterImpl extends QueryParameter {

  def getQueryParams(): java.util.List[String] = {
    val queryParams : java.util.List[String] = new java.util.ArrayList[String]()
    queryParams.add("id")
    queryParams.add("title")
    queryParams.add("body")
    queryParams.add("rank")
    queryParams.add("topic")
    queryParams.add("subtopic")
    queryParams
  }
}
