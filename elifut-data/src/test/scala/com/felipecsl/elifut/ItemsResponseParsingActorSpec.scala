package com.felipecsl.elifut

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model._
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.io.Source

class ItemsResponseParsingActorSpec(_system: ActorSystem)
    extends TestKit(_system)
        with Matchers
        with ImplicitSender
        with WordSpecLike
        with BeforeAndAfterAll {

  def this() = this(ActorSystem("ItemRootRequestActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "An ItemsResponseParsingActor" must {
    "parse the HTTP response" in {
      val json = Source.fromResource("item.json").mkString
      val httpResponse = HttpResponse(
        status = StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`application/json`, json)
      )
      val actor = system.actorOf(Props(classOf[ItemsResponseParsingActor]), "rootRequester")
      actor ! httpResponse
      val response = expectMsgType[ItemsResponse]
      response.totalPages should ===(908)
      response.totalResults should ===(21791)
    }
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
