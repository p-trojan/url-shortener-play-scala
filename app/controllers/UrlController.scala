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
import views.html.defaultpages.error

class UrlController @Inject()(urlRepository: UrlRepository,
                                  cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

    private val urls = mutable.ArrayBuffer[Url]().empty
    private val urlPostCall = routes.UrlController.addUrl
    private var lastUrl: Url = null
    private var found: String = ""
    
    getLastUrl

  val urlForm: Form[CreateUrlForm] = Form {
    mapping(
      "inputUrl" -> nonEmptyText
    )(CreateUrlForm.apply)(CreateUrlForm.unapply)
  }

  def index = Action { implicit request =>
    Ok(views.html.index(urlForm, urlPostCall, urls.toSeq, lastUrl))
  }

  def addUrl = Action.async { implicit request =>
    urlForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm, urlPostCall, urls.toSeq, lastUrl)))
      },
      url => {
        urlRepository.create(url.inputUrl, UUID.randomUUID().toString()).map { _ =>
          getLastUrl
          Redirect(routes.UrlController.index).flashing("success" -> "url.created")
        }
      }
    )
  }

  def decodeUrl(uuid: String) = Action.async { implicit request =>
    urlRepository.findByUUID(uuid).map { item =>
      Redirect(item.getOrElse(Url(0, "", "")).inputUrl)
    }
  }

  def getUrls = Action.async { implicit request =>
    urlRepository.list().map { urls =>
      Ok(Json.toJson(urls))
    }
  }

  def getLastUrl = urlRepository.getLast().map { last =>
    lastUrl = last.getOrElse(Url(-1, "NO_INPUT", "NO_OUTPUT"))
  }
}

case class CreateUrlForm(inputUrl: String)
