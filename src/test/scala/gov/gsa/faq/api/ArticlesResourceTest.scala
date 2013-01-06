package gov.gsa.faq.api

import model.{Articles, Article}
import org.scalatest.{FeatureSpec, BeforeAndAfter}
import gov.gsa.rest.api.{RangeFinder}
import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URI
import core._
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import io.Source
import scala.collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ArticlesResourceTest extends FeatureSpec with BeforeAndAfter {

  var articlesResource : ArticlesResource = _
  val uriInfo = mock(classOf[UriInfo])
  val request = mock(classOf[HttpServletRequest])
  val rangeFinder = mock(classOf[RangeFinder])

  before {

    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_EN.xml"), new File(Constants.XML_PATH))
    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_ES.xml"), new File(Constants.XML_PATH_ES))

    articlesResource = new ArticlesResourceJSON()
    articlesResource.uriInfo = uriInfo
    articlesResource.request = request
    articlesResource.rangeFinder = rangeFinder

    when(uriInfo.getRequestUri()).thenReturn(new URI("http://pants.nation.org:8080/usagovapi/contacts.json/contacts?pizza=cheese"))
    when(request.getRemoteAddr()).thenReturn("hamsandwich.com")
  }

  feature("getAllArticles") {

    scenario("no query params, result filters or sorts") {
      when(rangeFinder.getRange(2020, "items=1-1999")).thenReturn(new gov.gsa.rest.api.Range(1, 1999, 2020))

      val response : Response = articlesResource.getResource(null, null, null,"items=1-1999")
      val metadata : MultivaluedMap[String,Object] = response.getMetadata()
      assert("1-1999/2020" === metadata.get("X-Content-Range").get(0))

      val articles = response.getEntity().asInstanceOf[Articles].article
      assert(1999 === articles.size)

      var article : Article = null
      articles.foreach { _article =>
        if(_article.id == "9666") {
          article = _article
        }
      }

      article should not be (null)
      assert("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=9666" === article.link)
      assert(article.title.matches("Fish and Wildlife Service:.*Student Employment Programs"))
      assert("<![CDATA["+Source.fromInputStream(getClass().getResourceAsStream("/9666.body")).getLines().mkString("\n")+"]]" === article.body)
      assert(50.43334 === article.rank.toDouble)
      assert("Nov 26 2012 04:58:24:000PM" === article.updated)

      val topics = article.topics.topic
      assert(2 === topics.size)
      assert("Jobs and Education" === topics(0).name)
      assert("Fish and Wildlife Service (FWS)" === topics(1).name)

      assert("Education" === topics(0).subtopics.subtopic(0))
      assert("Jobs" === topics(0).subtopics.subtopic(1))
    }
  }
}