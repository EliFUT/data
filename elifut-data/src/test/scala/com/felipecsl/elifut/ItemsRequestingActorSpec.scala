package com.felipecsl.elifut

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import com.felipecsl.elifut.actors.ItemsRequestingActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class ItemsRequestingActorSpec(_system: ActorSystem)
    extends TestKit(_system)
        with Matchers
        with ImplicitSender
        with WordSpecLike
        with BeforeAndAfterAll {

  def this() = this(ActorSystem("ItemRootRequestActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "An ItemsRequestingActor" must {
    "make the HTTP request" in {
      implicit val materializer: ActorMaterializer =
        ActorMaterializer(ActorMaterializerSettings(system))
      implicit val context: ExecutionContext = system.dispatcher
      val json = """{"hello":"world"}"""
      val responseFn: HttpRequest => Future[HttpResponse] =
        _ => Future.successful {
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, json))
        }
      val props = Props(classOf[ItemsRequestingActor], responseFn)
      val actor = system.actorOf(props, "foo")
      actor ! "1"
      val response = expectMsgType[HttpResponse]
      response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        body.utf8String should ===(json)
      }
    }
  }
}
