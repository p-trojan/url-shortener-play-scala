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
import play.api.data.validation.Constraint
import play.api.data.validation.ValidationError
import play.api.data.validation.Valid
import play.api.data.validation.Invalid

class UrlController @Inject()(urlRepository: UrlRepository,
                                  cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

    private val urlPostCall = routes.UrlController.addUrl
    private var lastUrl: Url = null
    private var found: String = ""
        
    getLastUrl

    val https = """https://""".r
    val http = """http://""".r

    val protocolCheckConstraint: Constraint[String] = Constraint("constraints.protocolcheck") { plainText =>
      val errors = plainText match {
        case https() => Seq(ValidationError("URL protocol missing: https:// "))
        case http() => Seq(ValidationError("URL protocol missing: http:// "))
        case _            => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid("Invalid input string", errors)
      }
    }

  val urlForm: Form[CreateUrlForm] = Form {
    mapping(
      "inputUrl" -> nonEmptyText.verifying(protocolCheckConstraint)
    )(CreateUrlForm.apply)(CreateUrlForm.unapply)
  }

  def index = Action { implicit request =>
    Ok(views.html.index(urlForm, urlPostCall, lastUrl))
  }

  def addUrl = Action.async { implicit request =>
    urlForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(BadRequest(views.html.index(errorForm, urlPostCall, lastUrl)))
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
