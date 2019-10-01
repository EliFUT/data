package com.felipecsl.elifut

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable, Props}
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.felipecsl.elifut.actors.{ItemsResponseParsingActor, UrlDownloadRequest, UrlDownloadingActor}
import com.felipecsl.elifut.models.Club
import org.apache.commons.io.FileUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

object MainApp extends App {
  private val YEAR = "2020"
  Util.downloadClubsImages(s"../$YEAR/players/", s"../$YEAR/images/clubs/")
}

object Util {
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit private val dispatcher: ExecutionContext = system.dispatcher

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
      .map(byteStrings => byteStrings.map(bs => Source.fromFuture(Future.successful(bs))))
      .map(_.map(s => parsingActor.ask(s).mapTo[ItemsResponse]))
      .flatMap(Future.sequence(_))
      .map(_.flatMap(_.items))
      .map(_.map(_.club))
      .map(_.flatMap(createClubImageDownloadRequests))
      .map(_.map {
        e += 1
        downloadImage(_, destImagesPath, e)
      })
      .onComplete {
        case Failure(e) => throw e
      }
  }

  private case class ImageDownloadRequest(
    url: String,
    destPath: String
  )

  private def createClubImageDownloadRequests(club: Club): Seq[ImageDownloadRequest] = {
    Seq(
      ImageDownloadRequest(club.imageUrls.dark.small, s"${club.id}/dark/small"),
      ImageDownloadRequest(club.imageUrls.dark.medium, s"${club.id}/dark/medium"),
      ImageDownloadRequest(club.imageUrls.dark.large, s"${club.id}/dark/large"),
      ImageDownloadRequest(club.imageUrls.light.small, s"${club.id}/light/small"),
      ImageDownloadRequest(club.imageUrls.light.medium, s"${club.id}/light/medium"),
      ImageDownloadRequest(club.imageUrls.light.large, s"${club.id}/light/large"),
    )
  }

  private def downloadImage(req: ImageDownloadRequest, baseDestPath: String, delay: Int): Unit = {
    val actorRef = system.actorOf(Props(classOf[UrlDownloadingActor]), "downloader")
    val request = UrlDownloadRequest(
      uri = req.url,
      destFilePath = s"$baseDestPath/${req.destPath}/"
    )
    system.scheduler.scheduleOnce(delay milliseconds, actorRef, request)
  }
}
