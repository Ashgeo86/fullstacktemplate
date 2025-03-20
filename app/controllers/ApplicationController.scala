package controllers

import models.{APIError, Book, DataModel}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repositories.DataRepository
import services.LibraryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationController @Inject()(
                                       val controllerComponents: ControllerComponents,
                                       val dataRepository : DataRepository,
                                       val service : LibraryService,
                                       implicit val ec: ExecutionContext) extends BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel).map(_ => Created)
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
        dataRepository.read(id).map {
          case Right(data) => Ok(Json.toJson(data))
          case Left(_) => BadRequest("Bad request")
        }
    }

  def update(id: String): Action[JsValue] = Action.async (parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) => dataRepository.update(id, dataModel).map {
        case Right(_) =>
          Accepted(Json.toJson(dataModel))
        case Left(_) =>
          BadRequest
      }
      case JsError(_) => Future(BadRequest)
    }
  }

  def index(): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]] =
    dataRepository.collection.find().toFuture().map {
      case books: Seq[DataModel] => Right(books)
      case _ => Left(APIError.BadAPIResponse(404, "Books cannot be found"))
    }
//  def index(): Action[AnyContent] = Action.async { implicit request =>
//    dataRepository.index().map{
//      case Right(item: Seq[DataModel]) => if (item.length < 1 ) {
//      BadRequest}
//      else
//     {Ok {Json.toJson(item)}}
//      case Left(_) => BadRequest(Json.toJson("Unable to find any books"))
//    }
//  }

  def delete(id:String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>  if (dataModel._id == id) {
        dataRepository.create(dataModel).map(_ => Accepted)
      }
        else {
          Future(BadRequest)
        }
      case JsError(_) => Future(BadRequest)
    }
  }
  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
    service.getGoogleBook(search = search, term = term).value.map {
      case Right(book)=> Ok (Json.toJson(book))//Hint: This should be the same as before
      case Left(error) => BadRequest
    }
  }
//  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
//    println("request")
//    service.getGoogleBook(search = search, term = term).map  { book => Ok (Json.toJson(book))
//    }.recover {
//      case e: Exception => InternalServerError(Json.obj("error" -> e.getMessage))
//    }
//  }
}
