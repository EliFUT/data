package com.felipecsl.elifut.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.ask
import akka.util.Timeout
import com.felipecsl.elifut.ItemsResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ItemsFetchingActor(
    sendAndReceive: HttpRequest => Future[HttpResponse],
) extends Actor with ActorLogging {
  implicit private val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  private val system = context.system
  implicit private val dispatcher: ExecutionContext = system.dispatcher
  private val parsingActorClass = classOf[ItemsResponseParsingActor]
  private val requestingActorClass = classOf[ItemsRequestingActor]
  private val requesterProps = Props(requestingActorClass, sendAndReceive)
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
