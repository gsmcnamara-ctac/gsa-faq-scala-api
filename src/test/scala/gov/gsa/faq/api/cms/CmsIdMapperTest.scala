package gov.gsa.faq.api.cms

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
  val file = SystemUtils.getJavaIoTmpDir

  before {
    FileUtils.touch(new File(file, "1.2"))
    FileUtils.touch(new File(file, "3.4"))
    FileUtils.deleteQuietly(new File(file, "5.6"))
    FileUtils.deleteQuietly(new File(file, "5.7"))
    mapper.location = file
  }

  feature("get") {

    scenario("id doesn't exist") {
      mapper.get("5") should be(null)
    }

    scenario("id exists") {
      FileUtils.touch(new File(file, "5.6"))
      assert("6" === mapper.get("5"))
    }
  }

  feature("delete") {

    scenario("file exists") {

      FileUtils.touch(new File(file, "5.6"))
      mapper.delete("5")
      assert(!new File(file, "5.6").exists())
    }
  }

  feature("set") {

    scenario("id does not exists") {
      mapper.put("5", "6")
      assert(new File(file, "5.6").exists())
    }

    scenario("id already exists") {
      FileUtils.touch(new File(file, "5.6"))
      mapper.put("5", "7")
      assert(new File(file, "5.7").exists())
      assert(!new File(file, "5.6").exists())
    }
  }
}


