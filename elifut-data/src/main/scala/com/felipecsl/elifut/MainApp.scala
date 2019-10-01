package com.felipecsl.elifut

import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.felipecsl.elifut.actors.{ItemsResponseParsingActor, UrlDownloadRequest, UrlDownloadingActor}
import com.felipecsl.elifut.models.Club
import org.apache.commons.io.FileUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object MainApp extends App {
  private val YEAR = "2020"
  Util.downloadClubsImages(s"../$YEAR/players/", s"../$YEAR/images/clubs/")
}

object Util {
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit private val dispatcher: ExecutionContext = system.dispatcher
  private val actorRef = system.actorOf(Props(classOf[UrlDownloadingActor]), "downloader")
  private val logger = Logging(system, actorRef)

  def downloadPlayersJson(destFilesPath: String): IndexedSeq[Cancellable] = {
    val actorRef = system.actorOf(Props(classOf[UrlDownloadingActor]), "downloader")
    val baseUrl = "https://www.easports.com/fifa/ultimate-team/api/fut/item"
    var e = 0
    // TODO: Fetch total pages from items JSON page 1 instead of hardcoding here
    (1 to 908).map { i =>
      val request = UrlDownloadRequest(
        uri = baseUrl + "?jsonParamObject=%7B\"page\":" + i + "%7D",
        destFilePath = s"$destFilesPath/players$i.json"
      )
      e += 1
      system.scheduler.scheduleOnce((e * 500) milliseconds, actorRef, request)
    }
  }

  /** TODO write tests */
  def downloadClubsImages(playersJsonDirectory: String, destImagesPath: String): Unit = {
    val sourcePath = new File(playersJsonDirectory)
    val parsingActor = system.actorOf(Props(classOf[ItemsResponseParsingActor]), "rootRequester")
    var e = 0
    Future.successful(sourcePath)
      .map(_.listFiles())
      .map(_.toSeq)
      .map(_.map(FileUtils.readFileToByteArray))
      .map(_.map(ByteString.apply))
      .map(_.map(bs => Source.fromFuture(Future.successful(bs))))
      .map(_.map(parsingActor.ask(_).mapTo[ItemsResponse]))
      .flatMap(Future.sequence(_))
      .map(_.flatMap(_.items))
      .map(_.map(_.club))
      .map(_.flatMap(createClubImageDownloadRequests))
      .map(_.map { case (url, filename) =>
        val destFile = s"$destImagesPath$filename"
        if (!new File(destFile).exists()) {
          e += 1
          downloadImage(url, destFile, e * 500)
        } else {
          logger.info(s"Skipping $url since destination file already exists")
        }
      })
      .onComplete {
        case Failure(e) => throw e
        case Success(_) => ()
      }
  }

  private def createClubImageDownloadRequests(club: Club): Map[String, String] = {
    val urlToFile: String => String = new URL(_).getPath.split('/').last
    val images = Seq(
      club.imageUrls.dark.small,
      club.imageUrls.dark.medium,
      club.imageUrls.dark.large,
      club.imageUrls.light.small,
      club.imageUrls.light.medium,
      club.imageUrls.light.large
    )
    images.map(i => i -> urlToFile(i)).toMap
  }

  private def downloadImage(uri: String, destFilePath: String, delay: Int): String = {
    val request = UrlDownloadRequest(
      uri = uri,
      destFilePath = destFilePath
    )
    system.scheduler.scheduleOnce(delay milliseconds, actorRef, request)
    uri
  }
}
