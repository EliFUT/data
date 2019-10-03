package com.felipecsl.elifut.actors

import java.io.File

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success}

case class UrlDownloadRequest(
  uri: String,
  destFilePath: String,
)

/** Downloads data from a provided URI to a destination file (if it doesnt already exist) */
class UrlDownloadingActor extends Actor with ActorLogging {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  private val http = Http(context.system)

  override def receive: PartialFunction[Any, Unit] = {
    case request: UrlDownloadRequest =>
      val filePath = request.destFilePath
      val uri = request.uri
      val destFile = new File(filePath)
      if (destFile.exists()) {
        log.info(s"Skipping $filePath ($uri) since destination file already exists")
      } else {
        log.info(s"Downloading $uri to $filePath")
        http.singleRequest(HttpRequest(uri = uri)).onComplete {
          case Success(HttpResponse(StatusCodes.OK, _, entity, _)) =>
            entity.dataBytes
              .runFold(ByteString(""))(_ ++ _)
              .onComplete {
                case Success(str) => FileUtils.writeByteArrayToFile(destFile, str.toArray)
                case Failure(exception) => log.info(s"Failed to write file: $exception")
              }
        }
      }
      case Success(resp@HttpResponse(code, _, _, _)) =>
        log.info("Request failed, response code: " + code)
        resp.discardEntityBytes()
      case Failure(exception) =>
        log.info(s"Request failed: $exception")
  }
}
