package gov.gsa.faq.api

import cms.{CmsIdMapper, ArticlesCmsServices}
import com.wordnik.swagger.core.util.RestResourceUtil
import dao.{FaqDatabase, FaqDao}
import gov.gsa.rest.api.{RangeFinder, RestAPI}
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.{UriInfo, Context, Response, MediaType}
import com.wordnik.swagger.annotations.{Api, ApiParam, ApiOperation}
import gov.gsa.rest.api.dao.InMemoryHSQLDatabase
import collection.mutable.ListBuffer
import model.{Results,Result, Articles, Article}
import gov.gsa.rest.api.exception.ApiException
import scala.Array
import com.wordnik.swagger.jaxrs.Help
import javax.ws.rs._
import scala.collection.JavaConversions._

trait ArticlesResource extends RestResourceUtil with RestAPI with LogHelper {

  @Context var uriInfo :UriInfo = _
  @Context var request :HttpServletRequest = _
  var rangeFinder : RangeFinder = new RangeFinder()
  var cmsServices : ArticlesCmsServices = new ArticlesCmsServices()
  var cmsIdMapper : CmsIdMapper = new CmsIdMapper()

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

  @GET
  @ApiOperation(value = "Insert new articles or update existing article by id", notes = "")
  @Path("/articles/cms/update")
  def updateSelectedCmsArticles(@ApiParam(value = "Article ids. Ex. \"1234|4567\"", required = true) @QueryParam("article_ids") articleIds:String) :Response = {

    val faqDao = new FaqDao(InMemoryHSQLDatabase.getInstance(new FaqDatabase()).getDataSource())
    updateCmsArticles(faqDao,articleIds)
  }

  def updateCmsArticles(faqDao: FaqDao, articleIds:String) : Response = {

    val results = new ListBuffer[Result]()
    val ids = if(articleIds!=null && articleIds.length>0) articleIds.split("[|]") else null

    if (ids!=null && ids.length>0) {
      for (articleId <- ids) {
        val result = new Result()
        result.id = articleId
        val article = faqDao.getArticle(articleId)
        val cmsId = cmsIdMapper.get(article.id)
        if (cmsId==null) {
          result.operation = "insert"
          val _cmsId = cmsServices.createArticle(article)
          result.result = if (_cmsId==null) "failure" else "success"
          cmsIdMapper.add(article.id,_cmsId)
        } else {
          result.operation = "update"
          val success = cmsServices.updateArticle(article, cmsId)
          result.result = if (success) "success" else "failure"
          if(!success) {
            logger.error("update of article with id="+article.id+" failed")
          }
        }
        results += result
      }
    }
    Response.ok().entity(new Results(results)).build()
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