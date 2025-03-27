package controllers

import baseSpec.BaseSpecWithApplication
import models.DataModel
import org.apache.bcel.Repository
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication {

  val TestApplicationController = new ApplicationController(
    component, repository, service, repoService)(executionContext)


  private val dataModel: DataModel = DataModel(
    "abcd",
    "test name",
    "test description",
    100
  )

  private val badDataModel: DataModel = DataModel(
    "343",
    "test name",
    "test description",
    100
  )

  private val updatedDataModel: DataModel = DataModel(
    "fgh",
    "test name2",
    "test description",
    100
  )



  "ApplicationController .create" should {

    "create a book in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      status(createdResult) shouldBe Status.CREATED
      afterEach()
    }

    "return BadRequest" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson("dataModel"))
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      status(createdResult) shouldBe Status.BAD_REQUEST
      afterEach()
    }

  }

  "ApplicationController .read" should {

    "find a book in the database by id" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED
      val readResult: Future[Result] = TestApplicationController.read("abcd")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe dataModel
      afterEach()
    }

    "return a bad request error when an id doesn't exist" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      val readResult: Future[Result] = TestApplicationController.read("abf")(FakeRequest())
      status(readResult) shouldBe Status.BAD_REQUEST
      //contentAsJson(readResult).as[DataModel] shouldBe dataModel
      afterEach()
    }
  }

  "ApplicationController .delete" should {

    "delete a book in the database by id" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
//      val createdResult: Future[Result] = TestApplicationController.create()(request)
//      status(createdResult) shouldBe Status.CREATED

      val deleteResult: Future[Result] = TestApplicationController.delete("abcd")(request)

      status(deleteResult) shouldBe Status.ACCEPTED
      afterEach()
    }
    "return a bad request error when id isn't found" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      //val createdResult: Future[Result] = TestApplicationController.create()(request)
      //status(request) shouldBe Status.CREATED
      val deleteResult: Future[Result] = TestApplicationController.delete("abt")(request)

      status(deleteResult) shouldBe Status.BAD_REQUEST
      afterEach()
    }
  }
  "ApplicationController .update" should {

    "update a book in the database by id" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      status(createdResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPost("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel.copy(name="New name")))
      val updateResult: Future[Result] = TestApplicationController.update(dataModel._id)(updateRequest)

      status(updateResult) shouldBe Status.ACCEPTED
      contentAsJson(updateResult).as[JsValue] shouldBe updateRequest.body
      afterEach()
    }

    "not update a book in the database by id" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPost("/api/${dataModel._id}").withBody[JsValue](Json.toJson(updatedDataModel))
      val updateResult: Future[Result] = TestApplicationController.update(dataModel._id)(updateRequest)

      status(updateResult) shouldBe Status.BAD_REQUEST
      afterEach()
    }
  }

  "ApplicationController .index" should {

    "find all book in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED
      val indexResult: Future[Result] = TestApplicationController.index()(FakeRequest())

      status(indexResult) shouldBe Status.OK
      afterEach()
    }
    "not find any book in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val indexResult: Future[Result] = TestApplicationController.index()(FakeRequest())

      status(indexResult) shouldBe Status.BAD_REQUEST
      afterEach()
    }
  }


  override def beforeEach(): Unit = await(repository.deleteAll())
  override def afterEach(): Unit = await(repository.deleteAll())

//  "ApplicationController .index" should {
//
//    val result = TestApplicationController.index()(FakeRequest())
//
//    "return TODO" in {
//      status(result) shouldBe Status.NOT_IMPLEMENTED
//    }
//  }
//
//  "ApplicationController .create()" should {
//    val result = TestApplicationController.index()(FakeRequest())
//
//    "return TODO" in {
//      status(result) shouldBe Status.NOT_IMPLEMENTED
//    }
//  }
//
//  "ApplicationController .read()" should {
//    val result = TestApplicationController.index()(FakeRequest())
//
//    "return TODO" in {
//      status(result) shouldBe Status.NOT_IMPLEMENTED
//    }
//  }
//
//  "ApplicationController .update()" should {
//    val result = TestApplicationController.index()(FakeRequest())
//
//    "return TODO" in {
//      status(result) shouldBe Status.NOT_IMPLEMENTED
//    }
//  }
//
//  "ApplicationController .delete()" should {
//    val result = TestApplicationController.index()(FakeRequest())
//
//    "return TODO" in {
//      status(result) shouldBe Status.NOT_IMPLEMENTED
//    }
//  }

}
