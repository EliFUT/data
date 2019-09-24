package com.felipecsl.elifut

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.felipecsl.elifut.actors.UrlDownloadingActor
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

object MainApp extends App {
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit private val dispatcher: ExecutionContext = system.dispatcher
//  private val requestToFuture = (r: HttpRequest) => Http(system).singleRequest(r)
//  private val props = Props(classOf[PlayersFetchingActor], requestToFuture, timeout)
//  private val itemsFetchingActor = system.actorOf(props, "playersFetchingActor")
//  itemsFetchingActor.ask()
//      .mapTo[Seq[Player]]
//      .onComplete(println)
  private val actorRef = system.actorOf(Props(classOf[UrlDownloadingActor]), "downloader")
  val baseUrl = "https://www.easports.com/fifa/ultimate-team/api/fut/item"
  var e = 0
  (1 to 908).map { i =>
    val request = actors.UrlDownloadRequest(
      uri = baseUrl + "?jsonParamObject=%7B\"page\":" + i + "%7D",
      destFilePath = s"players$i.json"
    )
    e += 1
    system.scheduler.scheduleOnce((e * 500) milliseconds, actorRef, request)
  }
}