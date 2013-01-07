package gov.gsa.faq.api.cms

import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import java.io.File
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import org.apache.commons.lang.SystemUtils

@RunWith(classOf[JUnitRunner])
class CmsIdMapperTest extends FeatureSpec with BeforeAndAfter {

  val mapper = new CmsIdMapper()
  val file = new File(SystemUtils.getJavaIoTmpDir, "id.map")

  before {
    FileUtils.writeStringToFile(file, "1=2" + "\n")
    FileUtils.writeStringToFile(file, "3=4" + "\n", true)
    mapper.location = file
  }

  feature("get") {

    scenario("id doesn't exist") {
      mapper.get("5") should be(null)
    }

    scenario("id exists") {
      FileUtils.writeStringToFile(file, "5=6" + "\n")
      assert("6" === mapper.get("5"))
    }
  }

  feature("set") {

    scenario("id does not exists") {
      mapper.add("5", "6")
      val conf = ConfigFactory.parseFile(file)
      assert("6" === conf.getString("5"))
      val fileString = FileUtils.readFileToString(file)
      assert("1=2\n3=4\n5=6\n" === fileString)
    }

    scenario("id already exists") {
      FileUtils.writeStringToFile(file, "5=6" + "\n", true)
      mapper.add("5", "6")
      val conf = ConfigFactory.parseFile(file)
      assert("6" === conf.getString("5"))
      val fileString = FileUtils.readFileToString(file)
      assert("1=2\n3=4\n5=6\n" === fileString)
    }
  }
}


