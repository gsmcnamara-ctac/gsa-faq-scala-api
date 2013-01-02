package gov.gsa.faq.api.model

import gov.gsa.rest.api.model.ResultFilter

class ResultFilterImpl extends ResultFilter {

  def getResultFilters() : java.util.List[String] = {
    val resultFilters : java.util.List[String] = new java.util.ArrayList[String]()
    resultFilters.add("id")
    resultFilters.add("link")
    resultFilters.add("title")
    resultFilters.add("body")
    resultFilters.add("rank")
    resultFilters.add("updated")
    resultFilters.add("topic")
    resultFilters
  }
}
