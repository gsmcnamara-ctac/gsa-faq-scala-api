package gov.gsa.faq.api.cms

import com.ctacorp.rhythmyx.soap.Guid

class GuidFactory {
  def getNewRevisionGUID(id:Long) : Long = {
    val guid = new Guid(id)
    guid.setRevision(-1)
    guid.getGuid()
  }
}
