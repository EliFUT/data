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
import com.felipecsl.elifut.models.Player
import org.apache.commons.io.FileUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object MainApp extends App {
  private val YEAR = "2020"
  Util.downloadNationsImages(s"../$YEAR/players/", s"../$YEAR/images/nations/")
}

object Util {
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)
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

  def downloadClubsImages(playersJsonDirectory: String, destImagesPath: String): Unit = {
    downloadImages(playersJsonDirectory, destImagesPath, createClubImageDownloadRequests)
  }

  def downloadNationsImages(playersJsonDirectory: String, destImagesPath: String): Unit = {
    downloadImages(playersJsonDirectory, destImagesPath, createNationImageDownloadRequests)
  }

  /** TODO write tests */
  def downloadImages(
    playersJsonDirectory: String,
    destImagesPath: String,
    playerToImages: Player => Map[String, String]
  ): Unit = {
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
      .map(_.flatMap(playerToImages))
      .map(_.toSet)
      .map(_.map { case (url, filename) =>
        val destFile = s"$destImagesPath$filename"
        e += 1
        downloadImage(url, destFile, e * 500)
      })
      .onComplete {
        case Failure(e) => throw e
        case Success(_) => ()
      }
  }

  private def createClubImageDownloadRequests(player: Player): Map[String, String] = {
    val club = player.club
    // Url ends with eg "normal/240.png" or "dark/240.png"
    val clubUrlToFile: String => String = new URL(_)
      .getPath
      .split('/')
      .takeRight(2)
      .mkString("/")
    val images = Seq(
      club.imageUrls.dark.small,
      club.imageUrls.dark.medium,
      club.imageUrls.dark.large,
      club.imageUrls.light.small,
      club.imageUrls.light.medium,
      club.imageUrls.light.large
    )
    images.map(i => i -> clubUrlToFile(i)).toMap
  }

  private def createNationImageDownloadRequests(player: Player): Map[String, String] = {
    val nation = player.nation
    val nationUrlToFile: String => String = new URL(_)
      .getPath
      .split('/')
      .last
    val images = Seq(
      nation.imageUrls.small,
      nation.imageUrls.medium,
      nation.imageUrls.large,
    )
    images.map(i => i -> nationUrlToFile(i)).toMap
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
