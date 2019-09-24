package com.felipecsl.elifut

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model._
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.felipecsl.elifut.actors.PlayersFetchingActor
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
      val json = Source.fromResource("singleItemWithNamePlaceholder.json").mkString
      val responseFn: HttpRequest => Future[HttpResponse] =
        request => Future.successful {
          val uri = request.uri.toString()
          // Use the page number from the URL as the player name
          val finalJson = json.replace(
            "<PLAYER NAME PLACEHOLDER>",
            s"Player ${uri.substring(uri.indexOf("\":") + 2)}".replace("%7D", "")
          )
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, finalJson))
        }
      val props = Props(classOf[PlayersFetchingActor], responseFn, timeout)
      val actor = system.actorOf(props, "playerFetcher")
      actor ! "fetch"
      val response = expectMsgType[Seq[Player]]
      response.size should ===(10)
      response.map(_.name).sorted should ===(List(
        "Player 1", "Player 10", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6",
        "Player 7", "Player 8", "Player 9"))
    }
  }
}
