package spreadsheet

import IO._
import java.io.File

object AppRunner extends App {
  val filename = args.headOption getOrElse "input.txt"

  val inputFile = new File(filename)

  loadFile(inputFile).flatMap(Resolver.resolve).fold(println("error"))(IO.printResult)
}

