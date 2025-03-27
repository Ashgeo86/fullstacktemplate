package services

import models.{APIError, DataModel}
import org.mongodb.scala.result
import repositories.DataRepositoryTrait

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(val userRepository: DataRepositoryTrait)(implicit ec: ExecutionContext) {

  def index(): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]] =
    userRepository.index()

  def read(id: String): Future[Either[APIError.BadAPIResponse, Option[DataModel]]] =
    userRepository.read(id)

  def create(book: DataModel): Future[Either[APIError.BadAPIResponse, DataModel]] =
    userRepository.create(book)

  def update(id: String, book: DataModel): Future[Either[APIError.BadAPIResponse, result.UpdateResult]] =
    userRepository.update(id, book)

  def delete(id: String): Future[Either[APIError.BadAPIResponse, result.DeleteResult]] =
    userRepository.delete(id)

}

