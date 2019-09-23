package com.felipecsl.elifut

import scala.util.Success
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import spray.json.RootJsonFormat

import scala.concurrent.Future

class ItemsResponseParsingActor extends Actor with ActorLogging {
  import com.felipecsl.elifut.PlayerJsonProtocol._
  import context.dispatcher

  private final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  private implicit val itemsJsonFormat: RootJsonFormat[ItemsResponse] = jsonFormat6(ItemsResponse)

  def receive: PartialFunction[Any, Unit] = {
    case resp @ HttpResponse(StatusCodes.OK, _, _, _) =>
      val origSender = sender()
      Unmarshal(resp).to[ItemsResponse].onComplete(r => {
        origSender ! r.get
      })
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}

class ItemsRequestingActor(
    sendAndReceive: HttpRequest => Future[HttpResponse]
) extends Actor with ActorLogging {

  private val baseUrl = "https://www.easports.com/fifa/ultimate-team/api/fut/item"

  def receive: PartialFunction[Any, Unit] = {
    case page =>
      val uri = baseUrl + "?jsonParamObject=%7B\"page\":" + page + "%7D"
      sender() ! sendAndReceive(HttpRequest(uri = uri))
  }
}

object MainApp extends App {
//  implicit val system: ActorSystem = ActorSystem()
//  private val clazz = classOf[ItemsResponseParsingActor]
//  private val requestToFuture = (r: HttpRequest) => Http(system).singleRequest(r)
//  private val actorRef: ActorRef = system.actorOf(Props(clazz, 1, requestToFuture), "requester")
//  actorRef ! "ignored"
}