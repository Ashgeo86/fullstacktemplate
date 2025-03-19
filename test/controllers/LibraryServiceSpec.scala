import baseSpec.BaseSpec
import connectors.LibraryConnector
import models.{Book, DataModel}
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

  "getGoogleBook" should {
    val url: String = "testUrl"

    "return a book" in {
      (mockConnector.get[JsValue](_: String)(_: OFormat[JsValue], _: ExecutionContext))
        .expects(url, *, *)
        .returning(Future(gameOfThrones.as[JsValue]))
        .once()

      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "")) { result =>
        result shouldBe gameOfThrones
      }
    }
  }
}
