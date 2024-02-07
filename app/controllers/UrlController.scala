package controllers

import javax.inject._

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.util.Failure
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import java.util.UUID

class UrlController @Inject()(urlRepository: UrlRepository,
                                  cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

    private val urls = mutable.ArrayBuffer[Url]().empty
    private val urlPostCall = routes.UrlController.addUrl
    private var lastUrl: Url = null
    
    getLastUrl

  /**
   * The mapping for the url form.
   */
  val urlForm: Form[CreateUrlForm] = Form {
    mapping(
      "inputUrl" -> nonEmptyText
    )(CreateUrlForm.apply)(CreateUrlForm.unapply)
  }

  /**
   * The index action.
   */
  def index = Action { implicit request =>
    Ok(views.html.index(urlForm, urlPostCall, urls.toSeq, lastUrl))
  }

  /**
   * The add url action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on UrlRepository.
   */
  def addUrl = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    urlForm.bindFromRequest().fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the url creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm, urlPostCall, urls.toSeq, lastUrl)))
      },
      // There were no errors in the from, so create the url.
      url => {
        urlRepository.create(url.inputUrl, UUID.randomUUID().toString()).map { _ =>
          // If successful, we simply redirect to the index page.
          getLastUrl
          Redirect(routes.UrlController.index).flashing("success" -> "url.created")
        }

      }
    )
  }

  // def decodeUrl(id: String) = Action.async { implicit request =>

  // }

  /**
   * A REST endpoint that gets all the urls as JSON.
   */
  def getUrls = Action.async { implicit request =>
    urlRepository.list().map { urls =>
      Ok(Json.toJson(urls))
    }
  }

  def repoList = urlRepository.list().onComplete {
      case Success(value) => urls.addAll(value).toSeq
      case Failure(exception) => println(exception)
  }

  def getLastUrl = urlRepository.getLast().onComplete {
    case Success(value) => lastUrl = value.getOrElse(Url(0, "", ""))
    case Failure(exception) => println(exception)
  }

}

/**
 * The create url form.
 *
 * Generally for forms, you should define separate objects to your models, since forms very often need to present data
 * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
 * that is generated once it's created.
 */
case class CreateUrlForm(inputUrl: String)
