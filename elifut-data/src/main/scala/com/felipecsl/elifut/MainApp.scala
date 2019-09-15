package com.felipecsl.elifut

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import spray.json.RootJsonFormat

class RequestActor extends Actor with ActorLogging {
  import com.felipecsl.elifut.PlayerJsonProtocol._
  import context.dispatcher

  private final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  private val http = Http(context.system)

  private implicit val itemsJsonFormat: RootJsonFormat[ItemsResponse] = jsonFormat6(ItemsResponse)

  private val baseUrl =
    "https://www.easports.com/fifa/ultimate-team/api/fut/item"

  override def preStart(): Unit = {
    http.singleRequest(HttpRequest(uri = baseUrl + "?jsonParamObject=%7B\"page\":1%7D"))
      .pipeTo(self)
  }

  def receive: PartialFunction[Any, Unit] = {
    case resp@HttpResponse(StatusCodes.OK, _, _, _) =>
      Unmarshal(resp).to[ItemsResponse].onComplete(r => log.info(r.get.toString))
    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}

object MainApp extends App {
  implicit val system: ActorSystem = ActorSystem()
  private val actorRef: ActorRef = system.actorOf(Props[RequestActor], "requester")
  actorRef ! "foo"
}