package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.BSONObjectID

case class User(
                 _id: Option[String],
                 username: String,
                 password: String,
                 books: Option[List[Book]]
               )


object User {
  implicit val formats: OFormat[User] = Json.format[User]
}

