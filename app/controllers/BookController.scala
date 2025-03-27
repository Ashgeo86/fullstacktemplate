package controllers

import javax.inject._
import play.api.mvc._
import models.Book
import repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BookController @Inject()(
  val controllerComponents: ControllerComponents,
  userRepository: UserRepository
)(implicit ec: ExecutionContext) extends BaseController {

  def addToFavorites(): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userId) =>
        val formData = request.body.asFormUrlEncoded
        formData.map { data =>
          val book = createBookFromFormData(data)
          
          userRepository.read(userId).flatMap {
            case Right(user) =>
              val currentBooks = user.books.getOrElse(List.empty)
              // Check if book already exists in user's list
              if (!currentBooks.exists(_.isbn == book.isbn)) {
                val updatedBooks = currentBooks :+ book
                val updatedUser = user.copy(books = Some(updatedBooks))
                
                userRepository.update(userId, updatedUser).map {
                  case Right(_) => Redirect(routes.UserController.userPage)
                    .flashing("success" -> "Book added to your collection")
                  case Left(_) => Redirect(routes.HomeController.index())
                    .flashing("error" -> "Failed to add book to your collection")
                }
              } else {
                Future.successful(Redirect(routes.UserController.userPage)
                  .flashing("info" -> "This book is already in your collection"))
              }
            case Left(_) =>
              Future.successful(Redirect(routes.UserController.login)
                .flashing("error" -> "User not found. Please login again"))
          }
        }.getOrElse {
          Future.successful(BadRequest("Invalid form data"))
        }
      case None =>
        Future.successful(Redirect(routes.UserController.login)
          .flashing("error" -> "You must be logged in to add books"))
    }
  }
  
  def removeFromFavorites(): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userId) =>
        val formData = request.body.asFormUrlEncoded
        formData.flatMap(_.get("isbn").flatMap(_.headOption)) match {
          case Some(isbn) =>
            userRepository.read(userId).flatMap {
              case Right(user) =>
                val currentBooks = user.books.getOrElse(List.empty)
                val updatedBooks = currentBooks.filterNot(_.isbn == isbn)
                val updatedUser = user.copy(books = Some(updatedBooks))
                
                userRepository.update(userId, updatedUser).map {
                  case Right(_) => Redirect(routes.UserController.userPage)
                    .flashing("success" -> "Book removed from your collection")
                  case Left(_) => Redirect(routes.UserController.userPage)
                    .flashing("error" -> "Failed to remove book from your collection")
                }
              case Left(_) =>
                Future.successful(Redirect(routes.UserController.login)
                  .flashing("error" -> "User not found. Please login again"))
            }
          case None =>
            Future.successful(BadRequest("ISBN is required"))
        }
      case None =>
        Future.successful(Redirect(routes.UserController.login)
          .flashing("error" -> "You must be logged in to remove books"))
    }
  }
  
  private def createBookFromFormData(data: Map[String, Seq[String]]): Book = {
    Book(
      isbn = data.getOrElse("isbn", Seq("Unknown")).head,
      title = data.getOrElse("title", Seq("Unknown")).head,
      pageCount = data.getOrElse("pageCount", Seq("0")).head.toInt,
      imageLinks = data.getOrElse("imageLinks", Seq("/assets/images/no-cove.jpg")).head,
      description = data.getOrElse("description", Seq("No description available")).head
    )
  }
}

