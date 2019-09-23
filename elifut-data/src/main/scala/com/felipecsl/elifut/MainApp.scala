package com.felipecsl.elifut

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import com.felipecsl.elifut.actors.ItemsFetchingActor
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext

object MainApp extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit private val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val dispatcher: ExecutionContext = system.dispatcher
  private val requestToFuture = (r: HttpRequest) => Http(system).singleRequest(r)
  private val props = Props(classOf[ItemsFetchingActor], requestToFuture)
  private val itemsFetchingActor = system.actorOf(props, "fetcher")
  itemsFetchingActor.ask(1)
      .mapTo[ItemsResponse]
      .onComplete(println)
}