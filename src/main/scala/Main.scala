import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

object Main extends App with LazyLogging {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  val clients = Utils.readClients("clients.txt")
  val orders = Utils.readOrders("orders.txt")
  val exchange = system.actorOf(Props(new Exchange()), "exchange")

  clients.foreach(c => exchange ! c)
  Thread.sleep(1000)
  orders.foreach(o => exchange ! o)
  Thread.sleep(3000)
  exchange.ask(Show).mapTo[List[Client]].foreach { res =>
    logger.debug(res.mkString("\n"))
    Utils.writeToFile(res)
  }
  Thread.sleep(3000)
  system.terminate()

}
