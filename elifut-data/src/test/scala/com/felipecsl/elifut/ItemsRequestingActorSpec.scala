package com.felipecsl.elifut

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future

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
      val json = """{"hello":"world"}"""
      val response: HttpRequest => Future[HttpResponse] =
        _ => Future.successful {
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, json))
        }
      val props = Props(classOf[ItemsRequestingActor], response, system.dispatcher)
      val actor = system.actorOf(props, "foo")
      actor ! "1"
      expectMsgType[HttpResponse]
    }
  }
}
