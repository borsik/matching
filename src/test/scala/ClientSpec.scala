import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class ClientSpec
    extends TestKit(ActorSystem("MySpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Client actor" must {
    val clientRef =
      TestActorRef(ClientActor("client", 1000, 10, 10, 10, 10))
    val client = clientRef.underlyingActor
    "buy" in {
      clientRef ! Buy("A", 10, 10)
      client.balanceDollar.shouldEqual(900)
      client.balanceA.shouldEqual(20)
    }
    "sell" in {
      clientRef ! Sell("A", 10, 10)
      client.balanceDollar.shouldEqual(1000)
      client.balanceA.shouldEqual(10)
    }
  }

  "Exchange actor" must {
    implicit val timeout: Timeout = Timeout(5.seconds)
    val exchangeRef = TestActorRef(new Exchange())
    exchangeRef ! Client("C1", 1000, 10, 10, 10, 10)
    exchangeRef ! Client("C2", 2000, 10, 10, 10, 10)
    "create actors" in {
      val clients =
        Await.result(exchangeRef.ask(Show).mapTo[List[Client]], 5.seconds)
      clients.size.shouldEqual(2)
    }
    "transfer" in {
      exchangeRef ! Transfer("C1", "C2", "A", 10, 10)
      val clients =
        Await.result(exchangeRef.ask(Show).mapTo[List[Client]], 5.seconds)
      clients.find(_.name == "C1").map { c =>
        c.A.shouldEqual(0)
        c.dollars.shouldEqual(1100)
      }
      clients.find(_.name == "C2").map { c =>
        c.A.shouldEqual(20)
        c.dollars.shouldEqual(1900)
      }
    }
  }

}
