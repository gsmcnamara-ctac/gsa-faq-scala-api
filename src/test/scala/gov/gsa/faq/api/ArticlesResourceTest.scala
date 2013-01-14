package gov.gsa.faq.api

import cms.{CmsIdMapper, ArticlesCmsServices}
import dao.FaqDao
import model.{Results, Articles, Article, Result}
import org.scalatest.{FeatureSpec, BeforeAndAfter}
import gov.gsa.rest.api.RangeFinder
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

@RunWith(classOf[JUnitRunner])
class ArticlesResourceTest extends FeatureSpec with BeforeAndAfter {

  var articlesResource: ArticlesResource = _
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
    reset(uriInfo, request, rangeFinder, cmsServices, cmsIdMapper)

    when(uriInfo.getRequestUri()).thenReturn(new URI("http://pants.nation.org:8080/usagovapi/contacts.json/contacts?pizza=cheese"))
    when(request.getRemoteAddr()).thenReturn("hamsandwich.com")
  }

  feature("getAllArticles") {

    scenario("no query params, result filters or sorts") {
      when(rangeFinder.getRange(2020, "items=1-1999")).thenReturn(new gov.gsa.rest.api.Range(1, 1999, 2020))

      val response: Response = articlesResource.getResource(null, null, null, "items=1-1999")
      val metadata: MultivaluedMap[String, Object] = response.getMetadata()
      assert("1-1999/2020" === metadata.get("X-Content-Range").get(0))

      val articles = response.getEntity().asInstanceOf[Articles].article
      assert(1999 === articles.size)

      var article: Article = null
      articles.foreach {
        _article =>
          if (_article.id == "9666") {
            article = _article
          }
      }

      article should not be (null)
      assert("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=9666" === article.link)
      assert(article.title.matches("Fish and Wildlife Service:.*Student Employment Programs"))
      assert("<![CDATA[" + Source.fromInputStream(getClass().getResourceAsStream("/9666.body")).getLines().mkString("\n") + "]]" === article.body)
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

  feature("getSelectedCmsArticles") {

    scenario("get 2 artciles from the cms, one exists and one does not") {
      when(cmsIdMapper.get("1001")).thenReturn(null)
      when(cmsIdMapper.get("9666")).thenReturn("654")

      val article = new Article()
      article.id = "wow"
      when(cmsServices.getArticle(654l)).thenReturn(article)

      val response = articlesResource.getSelectedCmsArticles("1001|9666")
      val articles = response.getEntity().asInstanceOf[Articles].article
      assert(1 === articles.size)
      assert("wow" === articles(0).id)
      assert("EN"===articles(0).language)
    }
  }

  feature("updateSelectedCmsArticles") {

    scenario("get 2 articles from the dao where one exists and one does not exist in the cms id map file") {

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
      when(cmsServices.updateArticle(article2, "654")).thenReturn("999")

      val response = articlesResource.updateCmsArticles(dao, "123|456")
      val results: java.util.List[Result] = response.getEntity.asInstanceOf[Results].result
      assert(2 === results.size)
      assert("123" === results(0).id)
      assert("insert" === results(0).operation)
      assert("success" === results(0).result)
      assert("321" === results(0).cmsId)
      assert("456" === results(1).id)
      assert("update" === results(1).operation)
      assert("success" === results(1).result)
      assert("999" === results(1).cmsId)

      verify(cmsIdMapper).put("123", "321")
      verify(cmsIdMapper).put("456", "999")
    }

    scenario("update article failed") {

      val dao = mock(classOf[FaqDao])

      val article1 = new Article()
      article1.id = "123"

      when(dao.getArticle("123")).thenReturn(article1)
      when(cmsIdMapper.get("123")).thenReturn(null)

      when(cmsServices.createArticle(article1)).thenReturn(null)

      val response = articlesResource.updateCmsArticles(dao, "123")
      val results: java.util.List[Result] = response.getEntity.asInstanceOf[Results].result
      assert(1 === results.size)
      assert("123" === results(0).id)
      assert("insert" === results(0).operation)
      assert("failure" === results(0).result)
    }

    scenario("article ids string is empty or null") {
      var response = articlesResource.updateCmsArticles(null, null)
      assert(0 === response.getEntity.asInstanceOf[Results].result.size())
      response = articlesResource.updateCmsArticles(null, "")
      assert(0 === response.getEntity.asInstanceOf[Results].result.size())
    }
  }
}