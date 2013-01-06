package gov.gsa.faq.api.cms

import gov.gsa.faq.api.{LogHelper, Constants}
import com.typesafe.config.ConfigFactory
import java.io.File
import com.typesafe.config.ConfigException
import org.apache.commons.io.FileUtils

class CmsIdMapper extends LogHelper {

  def addOrGetId(id:String,cmsId:String) : String = {

    val conf = ConfigFactory.parseFile(new File(Constants.CMS_ID_MAP))
    var value = {
       try {
         conf.getString(id)
       } catch {
         case e: ConfigException.Missing => (null)
       }
    }
    if (value==null) {
      FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),id+"="+cmsId+"\n",true)
      value=cmsId
    }
    value
  }
}
