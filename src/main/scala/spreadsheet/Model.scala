package spreadsheet

object Model {
  type Spreadsheet = Map[CellIndex, Expression]
  type ResultSpreadsheet = Map[CellIndex, Long]

  trait Cell
  case class UnresolvedCell(expression: Expression) extends Cell
  case class ResolvedCell(result: Long) extends Cell

  trait Expression
  case class CellIndex(row: Int, column: Int) extends Expression
  case class Number(n: Long) extends Expression
  case class Func(name: String, range: CellRange) extends Expression
  case class BinaryOp(left: Expression, right: Expression) extends Expression
  case class CellRange(start: CellIndex, end: CellIndex)

  case class Command(index: CellIndex, expression: Expression)
}
