package com.felipecsl.elifut.actors

import java.io.{BufferedWriter, File, FileWriter}

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString

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
            .map(_.utf8String)
            .onComplete {
              case Success(str) => writeFile(request.destFilePath, str)
              case Failure(exception) => log.info(s"Failed to write file: $exception")
            }
        case Success(resp@HttpResponse(code, _, _, _)) =>
          log.info("Request failed, response code: " + code)
          resp.discardEntityBytes()
        case Failure(exception) =>
          log.info(s"Request failed: $exception")
      }
  }

  /** write a `String` to the `filename`. */
  private def writeFile(filename: String, s: String): Unit = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(s)
    bw.close()
  }
}
