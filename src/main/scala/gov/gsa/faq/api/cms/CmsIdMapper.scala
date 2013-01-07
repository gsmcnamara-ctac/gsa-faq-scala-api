package gov.gsa.faq.api.cms

import gov.gsa.faq.api.{LogHelper, Constants}
import java.io.File
import org.apache.commons.io.FileUtils

class CmsIdMapper extends LogHelper {

  var location = {
    val file = new File(Constants.CMS_ID_MAP_DIR)
    file.mkdir()
    file
  }

  def put(id: String, cmsId: String) {
    val _cmsId = get(id)
    if (_cmsId == null) {
      FileUtils.touch(new File(location, id + "." + cmsId))
    } else {
      new File(location, id + "." + _cmsId).delete()
      FileUtils.touch(new File(location, id + "." + cmsId))
    }
  }

  def delete(id: String) {
    val files = location.list()
    for (file <- files) {
      if (file.startsWith(id + ".")) {
        new File(location, file).delete()
      }
    }
  }

  def get(id: String): String = {

    var cmsId: String = null.asInstanceOf[String]
    val files = location.list()
    for (file <- files) {
      if (file.startsWith(id + ".")) {
        cmsId = file.drop(id.length + 1)
      }
    }
    cmsId
  }
}
