package spreadsheet

import java.io.File
import scalaz.Traverse
import scalaz.std.list._
import scalaz.std.option._

import Model._

object IO {

  def loadFile(file: File): Option[Spreadsheet] = Traverse[List].traverse(scala.io.Source.fromFile(file).getLines.toList)(Parser.parse).map(_.toMap)

  def printResult(spreadsheet: ResultSpreadsheet) =
    spreadsheet.toSeq.sortBy { case (index, _) => index.row -> index.column }.foreach {
      case (index, number) => println(s"${index.row}|${index.column} $number")
    }
}
