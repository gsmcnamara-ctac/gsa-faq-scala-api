package gov.gsa.faq.api.security

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers._
import org.mockito.Mockito._
import javax.ws.rs.WebApplicationException
import org.apache.commons.io.FileUtils
import java.io.File
import gov.gsa.faq.api.Constants
import javax.servlet.http.HttpServletRequest
import gov.gsa.rest.api.exception.UnauthorizedException

@RunWith(classOf[JUnitRunner])
class AuthFilterTest extends FeatureSpec with BeforeAndAfter {

  val authFilter: AuthFilter = new AuthFilter()
  val request = mock(classOf[HttpServletRequest])

  feature("filter") {

    scenario("containerRequest is null") {

      try {
        authFilter.filter(null)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("method is not POST") {

      when(request.getMethod).thenReturn("DONKEY")

      authFilter.filter(request)
    }

    scenario("auth header is missing") {

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn(null)

      try {
        authFilter.filter(request)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("basic auth is null") {

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn("")

      try {
        authFilter.filter(request)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("basic auth only has username") {

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn("Basic ZHVkZQ==")

      try {
        authFilter.filter(request)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("required role is not in tomcat-users") {

      val stream = getClass().getResourceAsStream("/tomcat-users-no-role.xml")
      println(stream)
      FileUtils.copyInputStreamToFile(stream, new File(Constants.TOMCAT_USERS))

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn("Basic ZHVkZTpEVURFIQ==")

      try {
        authFilter.filter(request)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("user not found in tomcat-users") {

      FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/tomcat-users-no-user.xml"), new File(Constants.TOMCAT_USERS))

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn("Basic ZHVkZTpEVURFIQ==")

      try {
        authFilter.filter(request)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("user has wrong password") {

      FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/tomcat-users-wrong-password.xml"), new File(Constants.TOMCAT_USERS))

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn("Basic ZHVkZTpEVURFIQ==")

      try {
        authFilter.filter(request)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("user is missing role") {

      FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/tomcat-users-missing-role.xml"), new File(Constants.TOMCAT_USERS))

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn("Basic ZHVkZTpEVURFIQ==")

      try {
        authFilter.filter(request)
        fail()
      }
      catch {
        case e: UnauthorizedException => assert(401 === e.getCode)
      }
    }

    scenario("user is good") {

      FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/tomcat-users.xml"), new File(Constants.TOMCAT_USERS))

      when(request.getMethod).thenReturn("POST")
      when(request.getHeader("Authorization")).thenReturn("Basic ZHVkZTpEVURFIQ==")

      authFilter.filter(request)
    }
  }
}
