package com.felipecsl.elifut

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable, Props}
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.felipecsl.elifut.actors.{ItemsResponseParsingActor, UrlDownloadRequest, UrlDownloadingActor}
import akka.pattern.ask
import com.felipecsl.elifut.models.Club
import org.apache.commons.io.FileUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object MainApp extends App {
}

object Util {
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit private val dispatcher: ExecutionContext = system.dispatcher

  def downloadPlayersJson(): IndexedSeq[Cancellable] = {
    val actorRef = system.actorOf(Props(classOf[UrlDownloadingActor]), "downloader")
    val baseUrl = "https://www.easports.com/fifa/ultimate-team/api/fut/item"
    var e = 0
    // TODO: Fetch total pages from items JSON page 1 instead of hardcoding here
    (1 to 908).map { i =>
      val request = UrlDownloadRequest(
        uri = baseUrl + "?jsonParamObject=%7B\"page\":" + i + "%7D",
        destFilePath = s"players$i.json"
      )
      e += 1
      system.scheduler.scheduleOnce((e * 500) milliseconds, actorRef, request)
    }
  }

  def downloadClubsImages(): Unit = {
    val year = "2020"
    val sourceJsonPath = s"../$year/players/"
    val sourcePath = new File(sourceJsonPath)
    val parsingActor = system.actorOf(Props(classOf[ItemsResponseParsingActor]), "rootRequester")
    sourcePath.listFiles()
      .map(FileUtils.readFileToByteArray)
      .map(ByteString.apply)
      .map(Future.successful)
      .map(Source.fromFuture)
      .foreach(
        parsingActor.ask(_)
          .mapTo[ItemsResponse]
          .map(_.items.map(i => i.club))
          .onComplete {
            case Success(clubs) => clubs.foreach(downloadClubImages)
            case Failure(exception) => throw exception
          }
      )
  }

  def downloadClubImages(club: Club) = {
    val year = "2020"
    val destImagesPath = s"../$year/images/clubs/"
    val downloadingActor = system.actorOf(Props(classOf[UrlDownloadingActor]), "downloader")
  }
}
