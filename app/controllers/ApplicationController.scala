package controllers

import akka.util.Helpers.Requiring
import models.{APIError, DataModel}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repositories.DataRepository
import services.{LibraryService, RepositoryService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Singleton

@Singleton
class ApplicationController @Inject()(
                                       val controllerComponents: ControllerComponents,
                                       val dataRepository : DataRepository,
                                       val service : LibraryService,
                                       val repositoryService: RepositoryService)(
                                       implicit val ec: ExecutionContext) extends BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel).map(_ => Created)
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.read(id).map {
      case Right(data) => Ok(Json.toJson(data))
      case Left(_) => BadRequest("Bad request")
    }
  }

  def update(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) => repositoryService.update(id, dataModel).map {
        case Right(_) =>
          Accepted(Json.toJson(dataModel))
        case Left(_) =>
          BadRequest("Cannot update")
      }
      case JsError(_) => Future(BadRequest("Cannot update"))
    }
  }

  def index(): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.index().map {
      case Right(item: Seq[DataModel]) => if (item.length < 1) {
        BadRequest("Unable to find any books")
      }
      else {
        Ok(views.html.example(item))
      }
      case Left(_) => BadRequest(Json.toJson("Unable to find any books"))
    }
  }

  def delete(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) => if (dataModel._id == id) {
        repositoryService.create(dataModel).map(_ => Accepted)
      }
      else {
        Future(BadRequest("cannot delete"))
      }
      case JsError(_) => Future(BadRequest("cannot delete"))
    }
  }

  def getGoogleBook(search: String): Action[AnyContent] = Action.async { implicit request =>

    service.getGoogleBook(search = search).value.map {
      case Right(book) => Ok(Json.toJson(book))
      case Left(_) => BadRequest(Json.toJson("Unable to find any books"))
    }
  }

  def hobbit(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.hobbit())
  }

  def googleBookSearch(search: String): Action[AnyContent] = Action.async { implicit request =>

    service.getGoogleBook(search = search).value.map {
      case Right(book) => Ok(views.html.searchResults(book))
      case Left(_) => BadRequest(Json.toJson("Unable to find any books"))
    }
  }
}


