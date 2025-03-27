package repositories

import models.{APIError, User}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{empty, equal}
import org.mongodb.scala.model._
import org.mongodb.scala.result
import play.api.libs.json.JsError
import reactivemongo.api.bson.BSONObjectID
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(
                                mongoComponent: MongoComponent
                              )(implicit ec: ExecutionContext) extends PlayMongoRepository[User](
  collectionName = "users",
  mongoComponent = mongoComponent,
  domainFormat = User.formats,
  indexes = Seq(IndexModel(
    Indexes.ascending("_id")
  )),
  replaceIndexes = false
) {

  def index(): Future[Either[APIError.BadAPIResponse, Seq[User]]]  =
    collection.find().toFuture().map{
      case user => Right(user)
      case _ => Left(APIError.BadAPIResponse(404, "User cannot be found"))
    }

  def create(user: User): Future[User] = {
    val userWithId = user.copy(_id = Some(BSONObjectID.generate().stringify), books = Some(List.empty))
    collection
      .insertOne(userWithId)
      .toFuture()
      .map(_ => userWithId)
  }

  private def byID(id: String): Bson =
    Filters.and(
      Filters.equal("_id", id)
    )

  def read(id: String): Future[Either[APIError.BadAPIResponse, User]] =
    collection.find(byID(id)).headOption.map {
      case Some(data) => Right(data)
      case _ => Left(APIError.BadAPIResponse(404, "User cannot be found"))
    }

  def update(id: String, user: User): Future[Either[APIError.BadAPIResponse, result.UpdateResult]] =
    collection.replaceOne(
      filter = byID(id),
      replacement = user,
      options = new ReplaceOptions().upsert(false) // Set to false for strict updates
    ).toFuture().map { result =>
      if (result.getMatchedCount == 0)
        Left(APIError.BadAPIResponse(404, "User not found"))
      else
        Right(result)
    }.recover {
      case _ => Left(APIError.BadAPIResponse(500, "User cannot be updated"))
    }


  def delete(id: String): Future[result.DeleteResult] =
    collection.deleteOne(
      filter = byID(id)
    ).toFuture()

  def deleteAll(): Future[Unit] = collection.deleteMany(empty()).toFuture().map(_ => ()) //Hint: needed for tests

  def validateCredentials(username: String, password: String): Future[Option[User]] = {
    // Example: Query the database for the provided username and password
    collection.find(Filters.and(equal("username", username), equal("password", password)))
      .headOption()
  }
}
