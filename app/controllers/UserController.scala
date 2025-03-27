package controllers

import controllers.routes
import models.{APIError, User}
import play.api.data.Form
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repositories.UserRepository
import services.UserService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Singleton

@Singleton
class                            UserController @Inject()(
                                val controllerComponents: ControllerComponents,
                                val userRepository : UserRepository,
                                val userService: UserService,
                                implicit val ec: ExecutionContext) extends BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User] match {
      case JsSuccess(user, _) =>
        userRepository.create(user).map(_ => Created)
      case JsError(_) => Future(BadRequest("Denied in controller"))
    }
  }

  def registerPage(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.register())
  }

  def register(): Action[AnyContent] = Action.async { implicit request =>
    val formData = request.body.asFormUrlEncoded

    formData match {
      case Some(data) =>
        val usernameOpt = data.get("username").flatMap(_.headOption)
        val passwordOpt = data.get("password").flatMap(_.headOption)

        (usernameOpt, passwordOpt) match {
          case (Some(username), Some(password)) =>
            // Check if username already exists
            userRepository.index().flatMap {
              case Right(users) =>
                if (users.exists(_.username == username)) {
                  Future.successful(Redirect(routes.UserController.registerPage())
                    .flashing("error" -> "Username already exists"))
                } else {
                  val newUser = User(None, username, password, None)
                  userRepository.create(newUser).map { user =>
                    Redirect(routes.UserController.login)
                      .flashing("success" -> "Registration successful. Please login.")
                  }
                }
              case Left(_) =>
                // If we can't check existing users, proceed with registration
                val newUser = User(None, username, password, None)
                userRepository.create(newUser).map { user =>
                  Redirect(routes.UserController.login)
                    .flashing("success" -> "Registration successful. Please login.")
                }
            }
          case _ =>
            Future.successful(Redirect(routes.UserController.registerPage())
              .flashing("error" -> "Please provide both username and password"))
        }
      case None =>
        Future.successful(Redirect(routes.UserController.registerPage())
          .flashing("error" -> "Invalid registration request"))
    }
  }

  def login(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login())
  }

  def validateLogin(): Action[AnyContent] = Action.async { implicit request =>
    val loginVals = request.body.asFormUrlEncoded

    // Extract login credentials (e.g., username and password)
    loginVals match {
      case Some(values) =>
        val usernameOpt = values.get("username").flatMap(_.headOption)
        val passwordOpt = values.get("password").flatMap(_.headOption)

        (usernameOpt, passwordOpt) match {
          case (Some(username), Some(password)) =>
            // Validate the user (this could involve querying the repository)
            userRepository.validateCredentials(username, password).map {
              case Some(user) =>
                // Create a session with the user ID
                Redirect(routes.UserController.userPage)
                  .withSession("userId" -> user._id.getOrElse(""), "username" -> user.username)
              case None =>
                // Invalid login attempt
                Redirect(routes.UserController.login)
                  .flashing("error" -> "Invalid username or password")
            }
          case _ =>
            // Missing credentials
            Future.successful(
              Redirect(routes.UserController.login)
                .flashing("error" -> "Please provide both username and password")
            )
        }
      case None =>
        // Invalid request payload
        Future.successful(
          Redirect(routes.UserController.login)
            .flashing("error" -> "Invalid login request")
        )
    }
  }

  def logout(): Action[AnyContent] = Action { implicit request =>
    Redirect(routes.HomeController.index()).withNewSession
      .flashing("success" -> "You have been logged out")
  }

  def userPage(): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userId) =>
        userRepository.read(userId).map {
          case Right(user) =>
            val books = user.books.getOrElse(List.empty)
            Ok(views.html.userPage(userId, user.username, books))
          case Left(_) =>
            Redirect(routes.UserController.login)
              .flashing("error" -> "User not found. Please login again")
              .withNewSession
        }
      case None =>
        Future.successful(Redirect(routes.UserController.login)
          .flashing("error" -> "You must be logged in to view this page"))
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    userRepository.read(id).map {
      case Right(data) => Ok(Json.toJson(data))
      case Left(_) => BadRequest("Bad request")
    }
  }

  def update(id: String): Action[JsValue] = Action.async (parse.json) { implicit request =>
    request.body.validate[User] match {
      case JsSuccess(user, _) => userRepository.update(id, user).map {
        case Right(_) =>
          Accepted(Json.toJson(user))
        case Left(_) =>
          BadRequest("Cannot update")
      }
      case JsError(_) => Future(BadRequest("Cannot update"))
    }
  }

  def index(): Action[AnyContent] = Action.async { implicit request =>
    userRepository.index().map{
      case Right(item: Seq[User]) => if (item.length < 1 ) {
        BadRequest("Unable to find any users")}
      else
      {Ok {Json.toJson(item)}}
      case Left(_) => BadRequest(Json.toJson("Unable to find any users"))
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async {
    userRepository.delete(id).map {
      case result if result.getDeletedCount > 0 => Accepted
      case _ => NotFound(s"User with ID $id not found")
    }
  }
}

