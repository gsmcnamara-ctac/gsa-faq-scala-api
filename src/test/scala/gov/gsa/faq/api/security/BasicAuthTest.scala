package gov.gsa.faq.api.security

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec

@RunWith(classOf[JUnitRunner])
class BasicAuthTest extends FeatureSpec {

  var basicAuth: BasicAuth = new BasicAuth()

  feature("decode") {

    scenario("auth is null") {

      val decoded = basicAuth.decode(null)
      assert(null == decoded)
    }

    scenario("auth has both username and password") {

      val decoded = basicAuth.decode("Basic ZHVkZTpEVURFIQ==")
      assert(2 === decoded.length)
      assert("dude" === decoded(0))
      assert("DUDE!" === decoded(1))
    }

    scenario("auth has only username") {

      val decoded = basicAuth.decode("Basic ZHVkZQ==")
      assert(1 === decoded.length)
      assert("dude" === decoded(0))
    }
  }
}
