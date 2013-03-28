package gov.gsa.faq.api.security

import com.sun.jersey.spi.container.ContainerRequestFilter
import gov.gsa.faq.api.Constants
import java.io.File
import collection.mutable.ListBuffer
import gov.gsa.rest.api.exception.UnauthorizedException
import javax.servlet.http.HttpServletRequest

class AuthFilter {

  val POST_ROLE = "faq-write"

  def filter(request: HttpServletRequest) = {

    def unauthorized: Nothing = {
      throw new UnauthorizedException(401, "Unauthorized")
    }

    if (request==null) {
      unauthorized
    }

    val method = request.getMethod

    if(method.equals("POST")) {
      val auth = request.getHeader("Authorization")
      if(auth == null){
        unauthorized
      }

      val lap = new BasicAuth().decode(auth)
      if(lap == null || lap.length != 2){
        unauthorized
      }

      if (!doAuth(lap(0),lap(1))) {
        unauthorized
      }
    }
  }

  def doAuth(username : String, password : String) : Boolean = {
    val rootElement = scala.xml.XML.loadFile(new File(Constants.TOMCAT_USERS))

    val roles  = new ListBuffer[String]()
    (rootElement \ "role").map {
      role =>
        roles += (role \ "@rolename").text
    }


    if (roles.contains(POST_ROLE)) {

      case class User(username: String, password: String, roles: Array[String]){}

      val users = (rootElement \ "user").map {
        user =>
          val username = (user \ "@username").text
          val password = (user \ "@password").text
          val roles = (user \ "@roles").text.split(",")
        User(username,password,roles)
      }

      for (user <- users) {
        if (user.username == username && user.password == password && user.roles.contains(POST_ROLE)) {
          return true
        }
      }
      false
    } else {
      false
    }
  }
}
