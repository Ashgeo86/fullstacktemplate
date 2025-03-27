package services

import connectors.LibraryConnector
import models.{APIError, Book, GoogleBook,ResultList, GoogleBooksResponse, IndustryIdentifier}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import cats.data.EitherT
import cats.implicits.toUnorderedFoldableOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Singleton


@Singleton
class LibraryService @Inject()(connector: LibraryConnector) {

  def getGoogleBook(urlOverride: Option[String] = None, search: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, ResultList] =
    connector.get[ResultList](urlOverride.getOrElse(s"https://www.googleapis.com/books/v1/volumes?q=$search:intitle"))

  def findISBN(identifiers: Seq[IndustryIdentifier]):String = {
    identifiers.find(_.`type` == "ISBN_13").map(_.identifier)
      .orElse(identifiers.find(_.`type` == "ISBN_10").map(_.identifier))
      .getOrElse(identifiers.headOption.map(_.identifier).getOrElse(""))
  }

  def getClosestMatches(search: String, term: String, maxResults: Int)(implicit ec: ExecutionContext): EitherT[Future, APIError, Seq[Book]] = {
    val formattedTerm = term.split("\\s+").map(_.trim).mkString("+")
    val orderBy = "orderBy=relevance"
    val max = s"maxResults=$maxResults"
    val query = s"$search:$formattedTerm"
    val url = s"https://www.googleapis.com/books/v1/volumes?q=$query&$maxResults&$orderBy"

    println(s"I am making a request to the Url: $url")

    val result = connector.get[GoogleBooksResponse](url)

    result.map { googleBooksResponse =>
      googleBooksResponse.items.map { googleBook =>

        println(s"here is the book you wanted: ${googleBook.volumeInfo.title}")

        Book(
          isbn = googleBook.volumeInfo.industryIdentifiers.find(_.`type` == "ISBN_13").map(_.identifier).getOrElse(""),
          title = googleBook.volumeInfo.title,
          pageCount = googleBook.volumeInfo.pageCount.getOrElse(0),
          imageLinks = googleBook.volumeInfo.imageLinks.mkString,
          description = googleBook.volumeInfo.description.getOrElse("No description provided")
//            description = googleBook.volumeInfo.description.getOrElse("No description provided")
        )
      }
    }
  }
}


