package gov.gsa.faq.api.model

import gov.gsa.rest.api.model.Sort
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer

class SortImpl extends Sort {

  def getSorts(): java.util.List[String] = {
    val sorts = new ListBuffer[String]()
    sorts += "id"
    sorts += "link"
    sorts += "title"
    sorts += "rank"
    sorts += "updated"
    sorts += "language"
    sorts.toList
  }
}