package spreadsheet

import io._
import java.io.File

object AppRunner extends App {
  val filename = args.headOption getOrElse "input.txt"
  val inputFile = new File(filename)

  val result = for {
    input <- loadFile(inputFile)
    resultSpreadsheet <- Resolver.resolve(input)
  } yield
    for((index, cell) <- resultSpreadsheet)
      yield s"${index.row}|${index.column} $cell"

  result.fold(println("error"))(_.foreach(println))
}

object TestApp extends App {
  println {
    2 to 1
  }
}
