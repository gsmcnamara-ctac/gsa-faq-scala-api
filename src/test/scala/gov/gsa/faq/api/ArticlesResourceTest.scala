package gov.gsa.faq.api

import cms.{CmsIdMapper, ArticlesCmsServices}
import dao.FaqDao
import model.{Articles, Article}
import org.scalatest.{FeatureSpec, BeforeAndAfter}
import gov.gsa.rest.api.{RangeFinder}
import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URI
import core._
import org.mockito.Mockito._
import io.Source
import scala.collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import gov.gsa.faq.api.model.Result

@RunWith(classOf[JUnitRunner])
class ArticlesResourceTest extends FeatureSpec with BeforeAndAfter {

  var articlesResource : ArticlesResource = _
  val uriInfo = mock(classOf[UriInfo])
  val request = mock(classOf[HttpServletRequest])
  val rangeFinder = mock(classOf[RangeFinder])
  val cmsServices = mock(classOf[ArticlesCmsServices])
  val cmsIdMapper = mock(classOf[CmsIdMapper])

  before {

    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_EN.xml"), new File(Constants.XML_PATH))
    FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/FAQ_ES.xml"), new File(Constants.XML_PATH_ES))

    articlesResource = new ArticlesResourceJSON()
    articlesResource.uriInfo = uriInfo
    articlesResource.request = request
    articlesResource.rangeFinder = rangeFinder
    articlesResource.cmsServices = cmsServices
    articlesResource.cmsIdMapper = cmsIdMapper
    reset(uriInfo,request,rangeFinder,cmsServices,cmsIdMapper)

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

  feature("updateSelectedCmsArticles") {

    scenario("get two articles from the dao where one exists and one does not exist in the cms id map file") {

      val dao = mock(classOf[FaqDao])

      val article1 = new Article()
      article1.id = "123"

      val article2 = new Article()
      article2.id = "456"

      when(dao.getArticle("123")).thenReturn(article1)
      when(dao.getArticle("456")).thenReturn(article2)
      when(cmsIdMapper.get("123")).thenReturn(null)
      when(cmsIdMapper.get("456")).thenReturn("654")

      when(cmsServices.createArticle(article1)).thenReturn("321")
      when(cmsServices.updateArticle(article2,"654")).thenReturn(false)

      val response = articlesResource.updateCmsArticles(dao,"123|456")
      val results : List[Result] = response.getEntity.asInstanceOf[List[Result]]
      assert(2===results.size)
      assert("123"===results(0).id)
      assert("insert"===results(0).operation)
      assert("success"===results(0).result)
      assert("456"===results(1).id)
      assert("update"===results(1).operation)
      assert("failure"===results(1).result)

      verify(cmsIdMapper).add("123","321")
    }

    scenario("article ids string is empty or null") {
      var response = articlesResource.updateCmsArticles(null,null)
      assert(List[Result]()===response.getEntity)
      response = articlesResource.updateCmsArticles(null,"")
      assert(List[Result]()===response.getEntity)
    }
  }
}