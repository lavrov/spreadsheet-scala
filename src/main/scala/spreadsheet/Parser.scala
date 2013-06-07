package spreadsheet

import scala.util.parsing.combinator._
import lexical.{StdLexical, Lexical}
import syntactical.StdTokenParsers

import Model._

object Parser {

  private val parser = new StdTokenParsers {
    type Tokens = StdLexical
    val lexical: Tokens = new Tokens

    lexical.delimiters ++= "|" :: "(" :: ")" :: ":" :: "+" :: Nil

    def number = numericLit ^^ (c => Number(c.toLong))

    def cellIndex = numericLit ~ "|" ~ numericLit ^^ {case r~_~c => CellIndex(r.toInt, c.toInt)}

    def range = cellIndex ~ ":" ~ cellIndex ^^ {case s~_~e => CellRange(s, e)}

    def fun = ident ~ ("(" ~> range <~ ")") ^^ {case i~range => Func(i, range)}

    def binary = nonRecursiveExpression ~ "+" ~ expression ^^ {case left~_~right => BinaryOp(left, right)}

    def nonRecursiveExpression = cellIndex | number | fun

    def expression: Parser[Expression] = binary | cellIndex | number | fun

    def parse(in: String) = phrase {
      cellIndex ~ expression ^^ {case index~exp => (index, exp)}
    }(new lexical.Scanner(in))
  }

  def parse(in: String) = parser.parse(in).map(Some(_)) getOrElse None
}
