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

/** Downloads data from a provided URI to a destination file */
class UrlDownloadingActor extends Actor with ActorLogging {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  private val http = Http(context.system)

  override def receive: PartialFunction[Any, Unit] = {
    case request: UrlDownloadRequest =>
      log.info(s"Requesting ${request.uri}...")
      http.singleRequest(HttpRequest(uri = request.uri)).onComplete {
        case Success(HttpResponse(StatusCodes.OK, _, entity, _)) =>
          entity.dataBytes
            .runFold(ByteString(""))(_ ++ _)
            .onComplete {
              case Success(str) => FileUtils.writeByteArrayToFile(
                new File(request.destFilePath), str.asByteBuffer.array()
              )
              case Failure(exception) => log.info(s"Failed to write file: $exception")
            }
        case Success(resp@HttpResponse(code, _, _, _)) =>
          log.info("Request failed, response code: " + code)
          resp.discardEntityBytes()
        case Failure(exception) =>
          log.info(s"Request failed: $exception")
      }
  }
}
