package com.felipecsl.elifut

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model._
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.felipecsl.elifut.actors.ItemsFetchingActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.io.Source

class ItemsFetchingActorSpec(_system: ActorSystem)
    extends TestKit(_system)
        with Matchers
        with ImplicitSender
        with WordSpecLike
        with BeforeAndAfterAll {

  def this() = this(ActorSystem("ItemRootRequestActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "An ItemsFetchingActor" must {
    "fetch and parse the HTTP response" in {
      val json = Source.fromResource("item.json").mkString
      val responseFn: HttpRequest => Future[HttpResponse] =
        _ => Future.successful {
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, json))
        }
      val timeout = Timeout(5, TimeUnit.SECONDS)
      val props = Props(classOf[ItemsFetchingActor], responseFn, timeout)
      val actor = system.actorOf(props, "fetcher")
      actor ! 1
      val response = expectMsgType[ItemsResponse]
      response.totalPages should ===(908)
      response.totalResults should ===(21791)
      response.items.size should ===(24)
    }
  }
}
