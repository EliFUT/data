package com.felipecsl.elifut

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContextExecutor

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

object MainApp extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  private val parsingActorClass = classOf[ItemsResponseParsingActor]
  private val requestingActorClass = classOf[ItemsRequestingActor]
  private val requestToFuture = (r: HttpRequest) => Http(system).singleRequest(r)
  private val requesterProps = Props(requestingActorClass, requestToFuture, dispatcher)
  private val requestingActorRef = system.actorOf(requesterProps, "requester")
  private val parsingActorRef: ActorRef = system.actorOf(Props(parsingActorClass), "parser")
  private val responseFuture = requestingActorRef.ask(1).mapTo[HttpResponse]
  responseFuture.flatMap(parsingActorRef.ask)
      .mapTo[ItemsResponse]
      .onComplete(r => println(r))
}