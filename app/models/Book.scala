package models

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}


case class Book(
                  isbn: String,
                 title: String,
                  pageCount: Int,
                  imageLinks: String,
                 description: String,

               ) {

}

object Book {
  implicit val formats: OFormat[Book] = Json.format[Book]

  def writes(book: Book): JsObject = Json.obj(
    "_id" -> book.isbn,
    "name" -> book.title,
    "description" -> book.description,
    "pageCount" -> book.pageCount
  )

  // Function to map Book to DataModel
  def toDataModel(book: Book): DataModel = {
    DataModel(
      _id = book.isbn,
      title = book.title,
      description = book.description,
      pageCount = book.pageCount
    )
  }
}


//TERM pulls in ISBN


//}