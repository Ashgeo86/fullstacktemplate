import baseSpec.BaseSpec
import cats.data.EitherT
import com.mongodb.reactivestreams.client.internal.OperationExecutorImpl
import connectors.LibraryConnector
import models.APIError.BadAPIResponse
import models.{APIError, Book, DataModel}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json, OFormat}
import services.LibraryService

import scala.concurrent.{ExecutionContext, Future}

class LibraryServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite {
  val mockConnector = mock[LibraryConnector]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testService = new LibraryService(mockConnector)

  val gameOfThrones: JsValue = Json.obj(
    "_id" -> "someId",
    "name" -> "A Game of Thrones",
    "description" -> "The best book!!!",
    "pageCount" -> 100
  )

  //"getClosetMatches" should {
    //val bookSearch = getClosestMatches("title", "Harry Potter", 5)
  //}


  "getGoogleBook" should {
    val url: String = "testUrl"

    "return a book" in {
      val testing = (mockConnector.get[Book](_: String)(_: OFormat[Book], _: ExecutionContext): EitherT[Future, APIError, Book])
        .expects(url, *, *)
        .returning(EitherT.rightT(gameOfThrones.as[Book]))
        .once()

      println(testing)

      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "").value) { result =>
        result shouldBe Right(gameOfThrones.as[Book])
      }
    }

    "return an error" in {
      val url: String = "testUrl"

      (mockConnector.get[Book](_: String)(_: OFormat[Book], _: ExecutionContext))
        .expects(url, *, *)
        .returning(EitherT.leftT(APIError.BadAPIResponse(500, "Could not connect")))
        .once()

      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "").value) { result =>
        result shouldBe Left(BadAPIResponse(500, "Could not connect"))
      }
    }
  }
}
