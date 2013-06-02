package spreadsheet

import scala.util.parsing.combinator._
import lexical.{StdLexical, Lexical}
import syntactical.StdTokenParsers

object Parser {

  private val parser = new StdTokenParsers {
    type Tokens = StdLexical
    val lexical: Tokens = new Tokens

    lexical.delimiters ++= "|" :: "(" :: ")" :: ":" :: "+" :: Nil

    def number = numericLit ^^ (c => Number(c.toInt))

    def cellIndex = number ~ "|" ~ number ^^ {case r~_~c => CellIndex(r.n, c.n)}

    def range = cellIndex ~ ":" ~ cellIndex ^^ {case s~_~e => CellRange(s, e)}

    def fun = ident ~ ("(" ~> range <~ ")") ^^ {case i~range => Func(i, range)}

    def binary = nonRecursiveExpression ~ "+" ~ expression ^^ {case left~_~right => BinaryOp(left, right)}

    def nonRecursiveExpression = cellIndex | number | fun

    def expression: Parser[Expression] = binary | cellIndex | number | fun

    def parse(in: String) = phrase {
      cellIndex ~ expression ^^ {case index~exp => Command(index, exp)}
    }(new lexical.Scanner(in))
  }

  def parse(in: String) = parser.parse(in).map(Some(_)) getOrElse None
}

trait Expression
case class CellIndex(row: Int, column: Int) extends Expression
case class Number(n: Int) extends Expression
case class Func(name: String, range: CellRange) extends Expression
case class BinaryOp(left: Expression, right: Expression) extends Expression
case class CellRange(start: CellIndex, end: CellIndex)

case class Command(index: CellIndex, expression: Expression)
