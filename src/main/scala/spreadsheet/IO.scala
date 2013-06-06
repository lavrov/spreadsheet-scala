package spreadsheet

import java.io.File
import scalaz.syntax.traverse._
import scalaz.syntax.std.list._
import scalaz.std.list._
import scalaz.std.option._
import concurrent._
import duration.Duration
import ExecutionContext.Implicits._

import Model._

object IO {

  def loadFile(file: File): Option[Spreadsheet] = {
    val r = Await.result(
      Future.sequence(scala.io.Source.fromFile(file).getLines().toList.map(line => future(Parser.parse(line)))),
      Duration.Inf
    ).sequence.map(_.toMap)
    r
  }

  def printResult(spreadsheet: ResultSpreadsheet) =
    spreadsheet.toSeq.sortBy { case (index, _) => index.row -> index.column }.foreach {
      case (index, number) => println(s"${index.row}|${index.column} $number")
    }
}
