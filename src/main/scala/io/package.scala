package spreadsheet

import scalaz._
import std.stream._
import std.option._
import java.io.File

package object io {
  type Input = Seq[Command]

  def loadFile(file: File): Option[Input] = Traverse[Stream].traverse(scala.io.Source.fromFile(file).getLines.toStream)(Parser.parse)
}