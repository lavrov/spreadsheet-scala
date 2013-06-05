package spreadsheet

import IO._
import java.io.File

object AppRunner extends App {
  val filename = args.headOption getOrElse "input.txt"
  val inputFile = new File(filename)

  def cellToString(index: CellIndex, cell: Cell) = s"${index.row}|${index.column} $cell"

  val result = for {
    input <- loadFile(inputFile)
    solvedSpreadsheet <- Resolver.resolve(input)
  }
    yield solvedSpreadsheet.map((cellToString _).tupled)

  result.fold(println("error"))(_.foreach(println))
}
