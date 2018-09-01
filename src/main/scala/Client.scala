import akka.actor.Actor

case class Client(name: String, dollars: Int, A: Int, B: Int, C: Int, D: Int)
case class Order(client: String,
                 op: String,
                 sec: String,
                 price: Int,
                 amount: Int)

case class Buy(security: String, price: Int, amount: Int)
case class Sell(security: String, price: Int, amount: Int)

case object Show

case object Order {
  def check(a: Order, b: Order): Boolean = {
    a.op != b.op && (a.sec, a.price, a.amount) == (b.sec, b.price, b.amount)
  }
}

case class ClientActor(name: String,
                       dollars: Int,
                       A: Int,
                       B: Int,
                       C: Int,
                       D: Int)
    extends Actor {

  var balanceDollar = dollars
  var balanceA = A
  var balanceB = B
  var balanceC = C
  var balanceD = D

  override def receive: Receive = {
    case b: Buy =>
      b.security match {
        case "A" => balanceA += b.amount
        case "B" => balanceB += b.amount
        case "C" => balanceC += b.amount
        case "D" => balanceD += b.amount
      }
      balanceDollar -= b.price * b.amount

    case s: Sell =>
      s.security match {
        case "A" => balanceA -= s.amount
        case "B" => balanceB -= s.amount
        case "C" => balanceC -= s.amount
        case "D" => balanceD -= s.amount
      }
      balanceDollar += s.price * s.amount

    case Show =>
      sender() ! Client(name,
                        balanceDollar,
                        balanceA,
                        balanceB,
                        balanceC,
                        balanceD)
  }
}
