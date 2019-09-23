package com.felipecsl.elifut

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.felipecsl.elifut.actors.{ItemsFetchingActor, PlayersFetchingActor}
import com.felipecsl.elifut.models.Player
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.io.Source

class PlayersFetchingActorSpec(_system: ActorSystem)
    extends TestKit(_system)
        with Matchers
        with ImplicitSender
        with WordSpecLike
        with BeforeAndAfterAll {

  def this() = this(ActorSystem("ItemRootRequestActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "An PlayersFetchingActor" must {
    "fetch all item pages" in {
      val timeout = Timeout(5, TimeUnit.SECONDS)
      // TODO: Return a different JSON for each page request so we can really test the pagination logic
      val json = Source.fromResource("item.json").mkString
      val responseFn: HttpRequest => Future[HttpResponse] =
        _ => Future.successful {
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, json))
        }
      val props = Props(classOf[PlayersFetchingActor], responseFn, timeout)
      val actor = system.actorOf(props, "playerFetcher")
      actor ! "fetch"
      val response = expectMsgType[Seq[Player]]
      response.size should ===(24 * 908) // items per page * total pages
      response.map(_.name).toSet should ===(Set(
        "Reus", "Cristiano Ronaldo", "Lewandowski", "Pelé", "Sergio Ramos", "Suárez", "Maradona",
        "Hazard", "Neymar Jr", "Messi", "Modrić"
      ))
    }
  }
}
