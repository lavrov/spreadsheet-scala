import concurrent._
import duration._
import java.io.{FileWriter, BufferedWriter}
import java.util.concurrent.ForkJoinPool
import scala.util.Random

object TestDataGenerator extends App {
  val rows = 10
  val columns = 10

  def randomOperation = Random.nextInt(300) match {
    case 0 => (Random.nextInt(rows) + 1) + "|" + (Random.nextInt(columns) + 1)
    case _ => Random.nextLong().abs.toString
  }

  val fileWriter = new BufferedWriter(new FileWriter("input.txt"))

  for {
    row <- 1 to rows
    column <-1 to columns
  }
    fileWriter.write {
      row + "|" + column + " " + List.fill(10)(randomOperation).mkString(" + ") + "\n"
    }

  fileWriter.close()
}
