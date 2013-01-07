package gov.gsa.faq.api.cms

import gov.gsa.faq.api.{LogHelper, Constants}
import com.typesafe.config.ConfigFactory
import java.io.File
import com.typesafe.config.ConfigException
import org.apache.commons.io.FileUtils

class CmsIdMapper extends LogHelper {

  def add(id:String,cmsId:String) {
    val _cmsId = get(id)
    if (_cmsId==null) {
      FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),id+"="+cmsId+"\n",true)
    }
  }

  def get(id:String) : String = {

    val conf = ConfigFactory.parseFile(new File(Constants.CMS_ID_MAP))
    val value = {
       try {
         conf.getString(id)
       } catch {
         case e: ConfigException.Missing => (null)
       }
    }
    value
  }
}
