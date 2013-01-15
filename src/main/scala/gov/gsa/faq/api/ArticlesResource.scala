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
import model.{Results, Result, Articles, Article}
import gov.gsa.rest.api.exception.ApiException
import scala.Array
import com.wordnik.swagger.jaxrs.Help
import javax.ws.rs._
import scala.collection.JavaConversions._

trait ArticlesResource extends RestResourceUtil with RestAPI with LogHelper {

  @Context var uriInfo: UriInfo = _
  @Context var request: HttpServletRequest = _
  var rangeFinder: RangeFinder = new RangeFinder()
  var cmsServices: ArticlesCmsServices = new ArticlesCmsServices()
  var cmsIdMapper: CmsIdMapper = new CmsIdMapper()

  @GET
  @ApiOperation(value = "Get all Aritcles", notes = "")
  @Path("/articles")
  override def getResource(@ApiParam(value = "Filter by query param. Ex. \"id::1001\" Accepted query_filter(s) are id|title|body|rank|topic|subtopic|language", required = false) @QueryParam("query_filter") queryFilter: String,
                           @ApiParam(value = "Limit results by property name. Ex. \"id|title|rank\" Accepted result_filter(s) are id|link|title|body|rank|updated|topic|language", required = false) @QueryParam("result_filter") resultFilter: String,
                           @ApiParam(value = "Sort by property name. Use '-' for descending. Ex. \"id|-rank\" Accepted sort(s) are id|link|title|rank|updated|language", required = false) @QueryParam("sort") sortParam: String,
                           @ApiParam(value = "Limit results by range. Ex. \"items=1-101\"", required = false) @HeaderParam("X-Range") rangeHeader: String): Response = {

    try {
      val faqDao = new FaqDao(InMemoryHSQLDatabase.getInstance(new FaqDatabase()).getDataSource())
      val articles = faqDao.getArticles(queryFilter, resultFilter, sortParam)
      var range = rangeFinder.getRange(articles.size, rangeHeader)
      val articleList = new ListBuffer[Article]()

      if (articles.size > 0) {
        if (range == null) {
          range = new gov.gsa.rest.api.Range(1, articles.size, articles.size)
        }
        for (i <- range.start - 1 until range.end) {
          articleList += articles(i)
        }
      }
      Response.ok().entity(new Articles(articleList)).header("X-Content-Range", range.toString).build()

    } catch {
      case e: Exception => throw new ApiException(e)
    }
  }

  @GET
  @ApiOperation(value = "Get CMS articles by id", notes = "")
  @Path("/articles/cms/get")
  def getSelectedCmsArticles(@ApiParam(value = "Article ids. Ex. \"1234|4567\"", required = true) @QueryParam("article_ids") articleIds: String): Response = {
    val articleList = new ListBuffer[Article]()
    val ids = if (articleIds != null && articleIds.length > 0) articleIds.split("[|]") else null
    if (ids != null && ids.length > 0) {
      for (articleId <- ids) {
        val cmsId = cmsIdMapper.get(articleId)
        if (cmsId != null) {
          val faqDao = new FaqDao(InMemoryHSQLDatabase.getInstance(new FaqDatabase()).getDataSource())
          val article = cmsServices.getArticle(cmsId.toLong)
          if (article != null) {
            article.language = faqDao.getArticle(articleId).language
            articleList += article
          }
        }
      }
    }
    Response.ok().entity(new Articles(articleList)).build()
  }

  @GET
  @ApiOperation(value = "Insert new CMS articles or update existing CMS articles by id", notes = "")
  @Path("/articles/cms/update")
  def updateSelectedCmsArticles(@ApiParam(value = "Article ids. Ex. \"1234|4567\"", required = true) @QueryParam("article_ids") articleIds: String): Response = {

    val faqDao = new FaqDao(InMemoryHSQLDatabase.getInstance(new FaqDatabase()).getDataSource())
    updateCmsArticles(faqDao, articleIds)
  }

  def updateCmsArticles(faqDao: FaqDao, articleIds: String): Response = {

    try {

      val results = new ListBuffer[Result]()
      val ids = if (articleIds != null && articleIds.length > 0) articleIds.split("[|]") else null

      if (ids != null && ids.length > 0) {
        for (articleId <- ids) {
          val result = new Result()
          result.id = articleId
          val article = faqDao.getArticle(articleId)
          if (article!=null) {
            val cmsId = cmsIdMapper.get(article.id)
            if (cmsId == null) {
              result.operation = "insert"
              val _cmsId = cmsServices.createArticle(article)
              result.result = if (_cmsId == null) "failure" else "success"
              result.cmsId = _cmsId
              cmsIdMapper.put(article.id, _cmsId)
            } else {
              result.operation = "update"
              val id = cmsServices.updateArticle(article, cmsId)
              result.result = if (id != null) "success" else "failure"
              if (id == null) {
                logger.error("update of article with id=" + article.id + " failed")
              } else {
                cmsIdMapper.put(article.id, id)
                result.cmsId = id
              }
            }
          } else {
            result.result = "article with id="+articleId+" does not exist"
          }
          results += result
        }
      }
      Response.ok().entity(new Results(results)).build()
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