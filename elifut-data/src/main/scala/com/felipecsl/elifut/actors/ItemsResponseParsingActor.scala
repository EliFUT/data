package com.felipecsl.elifut.actors

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.felipecsl.elifut.ItemsResponse
import spray.json.RootJsonFormat

class ItemsResponseParsingActor extends Actor with ActorLogging {

  import com.felipecsl.elifut.PlayerJsonProtocol._
  import context.dispatcher

  private final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  private implicit val itemsJsonFormat: RootJsonFormat[ItemsResponse] = jsonFormat6(ItemsResponse)

  def receive: PartialFunction[Any, Unit] = {
    case resp@HttpResponse(StatusCodes.OK, _, _, _) =>
      val origSender = sender()
      Unmarshal(resp).to[ItemsResponse].onComplete(r => {
        origSender ! r.get
      })
    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}