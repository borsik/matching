import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.pattern.pipe
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContextExecutor, Future}

case class Transfer(from: String,
                    to: String,
                    sec: String,
                    price: Int,
                    amount: Int)

class Exchange(implicit timeout: Timeout) extends Actor with LazyLogging {
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  val waitingList: ListBuffer[Order] = ListBuffer[Order]()

  override def receive: Receive = {
    case c: Client =>
      context.actorOf(
        Props(ClientActor(c.name, c.dollars, c.A, c.B, c.C, c.D)),
        c.name)

    case t: Transfer =>
      for {
        fromActor <- context.child(t.from)
        toActor <- context.child(t.to)
      } yield {
        fromActor ! Sell(t.sec, t.price, t.amount)
        toActor ! Buy(t.sec, t.price, t.amount)
      }

    case a: Order =>
      waitingList.find(b => Order.check(a, b)) match {
        case Some(b) =>
          logger.debug(s"$a X $b")
          if (a.op == "s") {
            self ! Transfer(a.client, b.client, a.sec, a.price, a.amount)
          } else {
            self ! Transfer(b.client, a.client, a.sec, a.price, a.amount)
          }
          waitingList -= b

        case None =>
          logger.debug(s"$a added to waiting list")
          waitingList += a
      }

    case Show =>
      val clients: Future[List[Client]] =
        Future
          .sequence(context.children.map(_.ask(Show).mapTo[Client]))
          .map(_.toList)
      pipe(clients) to sender()
  }
}
