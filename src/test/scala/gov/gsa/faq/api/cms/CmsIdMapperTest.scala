package gov.gsa.faq.api.cms

import com.typesafe.config.ConfigFactory
import gov.gsa.faq.api.Constants
import org.apache.commons.io.FileUtils
import java.io.File
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._

@RunWith(classOf[JUnitRunner])
class CmsIdMapperTest extends FeatureSpec with BeforeAndAfter {

  val mapper = new CmsIdMapper()

  before {
    FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),"1=2"+"\n")
    FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),"3=4"+"\n",true)
  }

  feature("get") {

    scenario("id doesn't exist") {
      mapper.get("5") should be (null)
    }

    scenario("id exists") {
      FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),"5=6"+"\n")
      assert("6"===mapper.get("5"))
    }
  }

  feature("set") {

    scenario("id does not exists") {
      mapper.add("5","6")
      val conf = ConfigFactory.parseFile(new File(Constants.CMS_ID_MAP))
      assert("6"===conf.getString("5"))
      val fileString = FileUtils.readFileToString(new File(Constants.CMS_ID_MAP))
      assert("1=2\n3=4\n5=6\n"===fileString)
    }

    scenario("id already exists") {
      FileUtils.writeStringToFile(new File(Constants.CMS_ID_MAP),"5=6"+"\n",true)
      mapper.add("5","6")
      val conf = ConfigFactory.parseFile(new File(Constants.CMS_ID_MAP))
      assert("6"===conf.getString("5"))
      val fileString = FileUtils.readFileToString(new File(Constants.CMS_ID_MAP))
      assert("1=2\n3=4\n5=6\n"===fileString)
    }
  }
}


