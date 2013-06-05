package spreadsheet

import java.io.File
import scalaz.Traverse
import scalaz.std.stream._
import scalaz.std.option._

import Model._

object IO {
  type Input = Seq[Command]

  def loadFile(file: File): Option[Input] = Traverse[Stream].traverse(scala.io.Source.fromFile(file).getLines.toStream)(Parser.parse)

  def printResult(spreadsheet: Spreadsheet) =
    spreadsheet.sortBy { case (index, _) => index.row -> index.column }.foreach {
      case (index, ResolvedCell(number)) => println(s"${index.row}|${index.column} $number")
    }
}
