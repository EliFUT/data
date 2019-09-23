package com.felipecsl.elifut.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.ask
import akka.util.Timeout
import com.felipecsl.elifut.ItemsResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ItemsFetchingActor(
    httpRequestFn: HttpRequest => Future[HttpResponse],
    implicit private val timeout: Timeout,
) extends Actor with ActorLogging {
  private val system = context.system
  implicit private val dispatcher: ExecutionContext = system.dispatcher
  private val parsingActorClass = classOf[ItemsResponseParsingActor]
  private val requestingActorClass = classOf[ItemsRequestingActor]
  private val requesterProps = Props(requestingActorClass, httpRequestFn)
  private val requestingActorRef = system.actorOf(requesterProps, "requester")
  private val parsingActorRef: ActorRef = system.actorOf(Props(parsingActorClass), "parser")

  override def receive: PartialFunction[Any, Unit] = {
    case page =>
      val s = sender()
      requestingActorRef.ask(page)
          .mapTo[HttpResponse]
          .flatMap(parsingActorRef.ask)
          .mapTo[ItemsResponse]
          .onComplete {
            case Success(response) => s ! response
            case Failure(exception) => s ! exception
          }
  }
}
