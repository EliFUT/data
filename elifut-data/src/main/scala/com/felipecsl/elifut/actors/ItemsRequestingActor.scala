package com.felipecsl.elifut.actors

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ItemsRequestingActor(
    httpRequestFn: HttpRequest => Future[HttpResponse],
) extends Actor with ActorLogging {

  implicit private val dispatcher: ExecutionContext = context.system.dispatcher
  private val baseUrl = "https://www.easports.com/fifa/ultimate-team/api/fut/item"

  def receive: PartialFunction[Any, Unit] = {
    case page =>
      val uri = baseUrl + "?jsonParamObject=%7B\"page\":" + page + "%7D"
      val s = sender()
      log.info(s"Requesting $uri...")
      httpRequestFn(HttpRequest(uri = uri)).onComplete {
        case Success(response) => s ! response
        case Failure(exception) => s ! exception
      }
  }
}