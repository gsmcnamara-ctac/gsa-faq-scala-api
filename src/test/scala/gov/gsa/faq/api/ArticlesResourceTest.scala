package gov.gsa.faq.api

import dao.{FaqDatabase, FaqDao}
import model.{Articles, Article}
import org.scalatest.{FeatureSpec, BeforeAndAfter}
import gov.gsa.rest.api.{RangeFinder, RestAPI}
import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import com.wordnik.swagger.annotations.{Api, ApiParam, ApiOperation}
import com.wordnik.swagger.core.util.RestResourceUtil
import gov.gsa.rest.api.dao.InMemoryHSQLDatabase
import collection.mutable.ListBuffer
import gov.gsa.rest.api.exception.ApiException
import org.apache.commons.io.FileUtils
import java.io.File
import com.wordnik.swagger.jaxrs.Help
import java.net.URI
import core._
import org.mockito.Mockito.mock
import org.mockito.Mockito.when

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
      assert("1-1999/2020" == metadata.get("X-Content-Range").get(0))

      val articles = response.getEntity().asInstanceOf[Articles].article
      assert(1999 == articles.size, articles.size)

//      Articles _articles = (Articles) response.getEntity();
//      List<Article> articles = _articles.getArticles();
//
//      assertEquals(2020, articles.size());
//
//      Article article = null;
//
//      for (Article _article : articles) {
//        if(_article.getId().equals("9666")) {
//          article = _article;
//        }
//      }
//
//      assertEquals("http://answers.usa.gov/system/web/view/selfservice/templates/USAgov/egredirect.jsp?p_faq_id=9666", article.getLink());
//      assertTrue(article.getTitle().matches("Fish and Wildlife Service:.*Student Employment Programs"));
//      assertEquals("<![CDATA["+IOUtils.toString(new ClassPathResource("9666.body").getInputStream())+"]]", article.getBody());
//      assertEquals(new Double(50.43334), article.getRank());
//      assertEquals("Nov 26 2012 04:58:24:000PM", article.getUpdated());
//
//      Topics topics = article.getTopics();
//      List<Topic> topicsList = topics.getTopics();
//      assertEquals(2, topicsList.size());
//      assertEquals("Jobs and Education", topicsList.get(0).getName());
//      assertEquals("Fish and Wildlife Service (FWS)", topicsList.get(1).getName());
//
//      Topic topic = topicsList.get(0);
//      Subtopics subtopics = topic.getSubtopics();
//      List<String> subtopicsList = subtopics.getSubtopics();
//      assertEquals("Education", subtopicsList.get(0));
//      assertEquals("Jobs", subtopicsList.get(1));
    }
  }
}

trait ArticlesResource extends RestResourceUtil with RestAPI with LogHelper {

  @Context var uriInfo :UriInfo = _
  @Context var request :HttpServletRequest = _
  var rangeFinder : RangeFinder = new RangeFinder()

  @GET
  @ApiOperation(value = "Get all Aritcles", notes = "")
  @Path("/articles")
  override def getResource(@ApiParam(value = "Filter by query param. Ex. \"title::Social Security Administration (SSA)|rank:gt:10.1\"", required = false) @QueryParam("query_filter") queryFilter:String,
                           @ApiParam(value = "Limit results by property name. Ex. \"id|title|rank\"", required = false) @QueryParam("result_filter") resultFilter:String,
                           @ApiParam(value = "Sort by property name. Use '-' for descending. Ex. \"id|-rank\"", required = false) @QueryParam("sort") sortParam:String,
                           @ApiParam(value = "Limit results by range. Ex. \"items=1-101\"", required = false) @HeaderParam("X-Range") rangeHeader:String) : Response = {

    try{
      val faqDao = new FaqDao(InMemoryHSQLDatabase.getInstance(new FaqDatabase()).getDataSource())
      val articles = faqDao.getArticles(queryFilter, resultFilter, sortParam)
      var range = rangeFinder.getRange(articles.size, rangeHeader)
      val articleList = new ListBuffer[Article]()

      if (articles.size>0) {
        if (range == null) {
          range = new gov.gsa.rest.api.Range(1, articles.size, articles.size)
        }
        for(i <- range.start-1 until range.end) {
          articleList += articles(i)
        }
      }

      Response.ok().entity(new Articles(articleList)).header("X-Content-Range", range.toString).build()

    } catch {
      case e: Exception => throw new ApiException(e)
    }
  }
}

@Path("/articles.json")
@Api(value = "/articles", description = "Articles operations")
@Produces(Array(MediaType.APPLICATION_JSON + ";charset=utf-8"))
class ArticlesResourceJSON extends Help with ArticlesResource
