import scala.io.Source
import java.nio.file.{Files, Path, Paths}

object Utils {

  def readClients(path: String): List[Client] = {
    Source.fromFile(path).getLines().toList.map { line =>
      line.trim.split("\\s+").toList match {
        case List(name, dollars, balA, balB, balC, balD) =>
          Client(name,
                 dollars.toInt,
                 balA.toInt,
                 balB.toInt,
                 balC.toInt,
                 balD.toInt)
      }
    }
  }

  def readOrders(path: String): List[Order] = {
    Source.fromFile(path).getLines().toList.map { line =>
      line.trim.split("\\s+").toList match {
        case List(client, op, sec, price, count) =>
          Order(client, op, sec, price.toInt, count.toInt)
      }
    }
  }

  def writeToFile(out: List[Client]): Path = {
    val content = out.mkString("\n").getBytes
    Files.write(Paths.get("result.txt"), content)
  }

}
