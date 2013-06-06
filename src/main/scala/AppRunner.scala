package spreadsheet

import algorithm.parallel.ParallelAlgorithm
import IO._
import java.io.File
import algorithm.{Algorithm, DepthFirstSingleThread}

object AppRunner extends BaseApp {
  solveWithAlgorithm(DepthFirstSingleThread)
}

object ParallelAppRunner extends BaseApp {
  solveWithAlgorithm(ParallelAlgorithm)
}

trait BaseApp extends App{
  def filename = args.headOption getOrElse "input.txt"

  def inputFile = new File(filename)

  def solveWithAlgorithm(algorithm: Algorithm) = {
    val start = System.currentTimeMillis()
    loadFile(inputFile).flatMap(algorithm.resolve).fold(println("error"))(IO.printResult)
    println("Time spent: " + (System.currentTimeMillis() - start))
  }
}