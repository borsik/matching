import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(5.seconds)

  //  val clients = List(Client("C1", 1000, 5, 5, 5, 5), Client("C2", 1000, 5, 5, 5, 5))
  //  val orders = List(Order("C1", "b", "A", 1, 2), Order("C2", "s", "A", 1, 2))

  val clients = Utils.readClients("/Users/olzhas/Matching/clients.txt")
  val orders = Utils.readOrders("/Users/olzhas/Matching/orders.txt")
  val exchange = system.actorOf(Props(new Exchange()), "exchange")

  clients.foreach(c => exchange ! c)
  Thread.sleep(3000)
  orders.foreach(o => exchange ! o)
  Thread.sleep(3000)
  exchange.ask(Show).mapTo[List[Client]].foreach { res =>
    println(res)
    Utils.writeToFile(res)
  }
  Thread.sleep(3000)
  system.terminate()

}
