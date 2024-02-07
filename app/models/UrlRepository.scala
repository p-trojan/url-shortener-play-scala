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
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  /**
   * Here we define the table. It will have a inputUrl of urls
   */
  private class UrlsTable(tag: Tag) extends Table[Url](tag, "urls") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The inputUrl column */
    def inputUrl = column[String]("inputUrl")

    /** The outputUrl column */
    def outputUrl = column[String]("outputUrl")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Url object.
     *
     * In this case, we are simply passing the id, inputUrl and poutputUrl parameters to the Url case classes
     * apply and unapply methods.
     */
    def * = (id, inputUrl, outputUrl) <> ((Url.apply _).tupled, Url.unapply)
  }

  /**
   * The starting point for all queries on the urls table.
   */
  private val urls = TableQuery[UrlsTable]

  /**
   * Create a person with the given inputUrl and outputUrl.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(inputUrl: String, outputUrl: String): Future[Url] = db.run {
    // We create a projection of just the inputUrl and outputUrl columns, since we're not inserting a value for the id column
    (urls.map(u => (u.inputUrl, u.outputUrl))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning urls.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((inputOutputUrl, id) => Url(id, inputOutputUrl._1, inputOutputUrl._2))
    // And finally, insert the person into the database
    ) += (inputUrl, outputUrl)
  }

  /**
   * List all the urls in the database.
   */
  def list(): Future[Seq[Url]] = db.run {
    urls.result
  }

  def getLast(): Future[Option[Url]] = db.run {
    urls.sortBy(_.id.desc).result.headOption
  }
}
