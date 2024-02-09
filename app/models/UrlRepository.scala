package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ Future, ExecutionContext }
import models.Url

/**
 * A repository for urls.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class UrlRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class UrlsTable(tag: Tag) extends Table[Url](tag, "urls") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def inputUrl = column[String]("inputUrl")
    def outputUrl = column[String]("outputUrl")
    def * = (id, inputUrl, outputUrl) <> ((Url.apply _).tupled, Url.unapply)
  }

  private val urls = TableQuery[UrlsTable]

  def create(inputUrl: String, outputUrl: String): Future[Url] = db.run {
    (urls.map(u => (u.inputUrl, u.outputUrl))
      returning urls.map(_.id)
      into ((inputOutputUrl, id) => Url(id, inputOutputUrl._1, inputOutputUrl._2))
    ) += (inputUrl, outputUrl)
  }

  def list(): Future[Seq[Url]] = db.run {
    urls.result
  }

  def getLast(): Future[Option[Url]] = db.run {
    urls.sortBy(_.id.desc).result.headOption
  }

  def findByUUID(uuid: String): Future[Option[Url]] = db.run {
    urls.filter(_.outputUrl === uuid).result.headOption
  }
}
