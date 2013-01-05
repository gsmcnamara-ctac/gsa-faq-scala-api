package gov.gsa.faq.api

import com.wordnik.swagger.core.util.RestResourceUtil
import dao.{FaqDatabase, FaqDao}
import gov.gsa.rest.api.{RangeFinder, RestAPI}
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.{UriInfo, Context, Response, MediaType}
import com.wordnik.swagger.annotations.{Api, ApiParam, ApiOperation}
import gov.gsa.rest.api.dao.InMemoryHSQLDatabase
import collection.mutable.ListBuffer
import model.{Articles, Article}
import gov.gsa.rest.api.exception.ApiException
import scala.Array
import com.wordnik.swagger.jaxrs.Help
import javax.ws.rs._
import scala.collection.JavaConversions._


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

@Path("/articles.jsonp")
@Api(value = "/articles", description = "Articles operations")
@Produces(Array(MediaType.APPLICATION_JSON + ";charset=utf-8"))
class ArticlesResourceJSONP extends Help with ArticlesResource

@Path("/articles.xml")
@Api(value = "/articles", description = "Articles operations")
@Produces(Array(MediaType.APPLICATION_XML + ";charset=utf-8"))
class ArticlesResourceXML extends Help with ArticlesResource