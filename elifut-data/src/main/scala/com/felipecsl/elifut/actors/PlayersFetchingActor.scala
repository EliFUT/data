package com.felipecsl.elifut.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.ask
import akka.util.Timeout
import com.felipecsl.elifut.ItemsResponse
import com.felipecsl.elifut.models.Player

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PlayersFetchingActor(
    httpRequestFn: HttpRequest => Future[HttpResponse],
    implicit private val timeout: Timeout,
) extends Actor with ActorLogging {
  private val system = context.system
  implicit private val dispatcher: ExecutionContext = system.dispatcher
  private val fetchingActorClass = classOf[ItemsFetchingActor]
  private val props = Props(fetchingActorClass, httpRequestFn, timeout)
  private val itemsFetchingActor = system.actorOf(props, "fetcher")

  override def receive: PartialFunction[Any, Unit] = {
    case _ =>
      val s = sender()
      itemsFetchingActor.ask(1)
          .mapTo[ItemsResponse]
          .onComplete {
            case Success(response) => {
              val allPages = (1 to response.totalPages)
                  .map(page => itemsFetchingActor.ask(page)
                      .mapTo[ItemsResponse]
                      .map(_.items))
              Future.foldLeft(allPages)(Seq.empty[Player])(_ ++ _).onComplete {
                case Success(players) => s ! players
                case Failure(exception) => s ! exception
              }
            }
            case Failure(exception) => s ! exception
          }
  }
}
