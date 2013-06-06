import concurrent._
import duration._
import java.io.{FileWriter, BufferedWriter}
import java.util.concurrent.ForkJoinPool
import scala.util.Random

object TestDataGenerator extends App {
  val fileWriter = new BufferedWriter(new FileWriter("input.txt"))

  for {
    row <- 1 to 1000
    column <-1 to 100
  }
    fileWriter.write {
      row + "|" + column + " " + List.fill(20)(Random.nextLong().abs).mkString(" + ") + "\n"
    }

  fileWriter.close()
}
