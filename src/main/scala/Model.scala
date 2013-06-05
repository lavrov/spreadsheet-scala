package spreadsheet

object Model {
  type Spreadsheet = Seq[(CellIndex, Cell)]

  trait Cell
  case class UnresolvedCell(expression: Expression) extends Cell
  case class ResolvedCell(result: Int) extends Cell

  trait Expression
  case class CellIndex(row: Int, column: Int) extends Expression
  case class Number(n: Int) extends Expression
  case class Func(name: String, range: CellRange) extends Expression
  case class BinaryOp(left: Expression, right: Expression) extends Expression
  case class CellRange(start: CellIndex, end: CellIndex)

  case class Command(index: CellIndex, expression: Expression)
}
