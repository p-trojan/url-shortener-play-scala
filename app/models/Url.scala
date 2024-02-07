package models

import play.api.libs.json._

case class Url(id: Long, inputUrl: String, outputUrl: String)

object Url {  
  implicit val urlFormat: OFormat[Url] = Json.format[Url]
}
