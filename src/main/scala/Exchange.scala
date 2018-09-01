import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.pattern.pipe

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

case class Transfer(from: String,
                    to: String,
                    security: String,
                    price: Int,
                    amount: Int)

class Exchange(implicit timeout: Timeout) extends Actor {
  implicit val ec = context.dispatcher

  val waitingList = ListBuffer[Order]()

  override def receive: Receive = {
    case c: Client =>
      context.actorOf(
        Props(new ClientActor(c.name, c.dollars, c.A, c.B, c.C, c.D)),
        c.name)

    case t: Transfer =>
      for {
        fromActor <- context.child(t.from)
        toActor <- context.child(t.to)
      } yield {
        fromActor ! Sell(t.security, t.price, t.amount)
        toActor ! Buy(t.security, t.price, t.amount)
      }

    case a: Order =>
      waitingList.find(b => Order.check(a, b)) match {
        case Some(b) =>
          println(s"$a X $b")
          if (a.op == "s") {
            self ! Transfer(a.client, b.client, a.sec, a.price, a.amount)
          } else {
            self ! Transfer(b.client, a.client, a.sec, a.price, a.amount)
          }
          waitingList -= b

        case None =>
          println(s"$a added to waiting list")
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
