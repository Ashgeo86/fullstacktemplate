package controllers


import models.{Book, ResultList}
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.LibraryService

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
                                val controllerComponents: ControllerComponents,
                                val service : LibraryService,
                                ws: WSClient
                              )(implicit ec: ExecutionContext) extends BaseController {
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.index()))
  }

  def search(): Action[AnyContent] = Action.async { implicit request =>
    val query = request.getQueryString("query").getOrElse("")
    service.getGoogleBook(search = query).value.map {
      case Right(book) => Ok(views.html.bookShow(book))
      case Left(_) => BadRequest(Json.toJson("Unable to find any books"))
    }
  }

//  def bookSearch(books: ResultList):List[Book] = {
//    for (book <- books.items) yield {}
//  }

//  def search(): Action[AnyContent] = Action.async { implicit request =>
//    val query = request.getQueryString("query").getOrElse("")
//
//    if (query.isEmpty) {
//      Future.successful(Redirect(routes.HomeController.index()))
//    } else {
//      val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
//      val url = s"https://www.googleapis.com/books/v1/volumes?q=$encodedQuery"
//
//      ws.url(url).get().map { response =>
//        val books = parseGoogleBooksResponse(response.json)
//        Ok(views.html.bookShow(books))
//      }
//    }
//  }

//
//
}

