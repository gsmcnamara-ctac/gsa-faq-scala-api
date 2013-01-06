package gov.gsa.faq.api.cms

import com.typesafe.config.ConfigFactory
import gov.gsa.faq.api.Constants
import org.apache.commons.io.FileUtils
import java.io.File
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class CmsIdMapperTest extends FeatureSpec with BeforeAndAfter {

  val mapper = new CmsIdMapper()

  before {
    FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),"1=2"+"\n")
    FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),"3=4"+"\n",true)
  }

  feature("addOrGetId") {

    scenario("id doesn't exist") {
      assert("6"===mapper.addOrGetId("5","6"))
      val conf = ConfigFactory.parseFile(new File(Constants.CMS_ID_MAP))
      assert("6"===conf.getString("5"))
    }

    scenario("id exists") {
      assert("4"===mapper.addOrGetId("3","4"))
      val conf = ConfigFactory.parseFile(new File(Constants.CMS_ID_MAP))
      assert("4"===conf.getString("3"))
      val fileString = FileUtils.readFileToString(new File(Constants.CMS_ID_MAP))
      assert("1=2\n3=4\n"===fileString)
    }
  }
}


