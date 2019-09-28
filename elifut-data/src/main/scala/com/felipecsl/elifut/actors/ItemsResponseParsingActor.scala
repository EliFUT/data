package com.felipecsl.elifut.actors

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{JsonFraming, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import com.felipecsl.elifut.ItemsResponse
import spray.json.{RootJsonFormat, _}

/** Deserializes a {{{Source[ByteString, Any}}} into a {{{ItemsResponse}}} object */
class ItemsResponseParsingActor extends Actor
  with ActorLogging
  with SprayJsonSupport
  with DefaultJsonProtocol {

  import com.felipecsl.elifut.PlayerJsonProtocol._
  import context.dispatcher

  private final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json()

  private implicit val itemsJsonFormat: RootJsonFormat[ItemsResponse] = jsonFormat6(ItemsResponse)

  def receive: PartialFunction[Any, Unit] = {
    case resp: Source[ByteString, Any] =>
      val origSender = sender()
      resp.via(JsonFraming.objectScanner(Int.MaxValue))
        .mapAsync(1)(bytes => Unmarshal(bytes).to[ItemsResponse])
        .runWith(Sink.head)
        .onComplete(r => origSender ! r.get)
    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}