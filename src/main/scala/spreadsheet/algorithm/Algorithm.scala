package spreadsheet.algorithm

import spreadsheet._
import Model._
import IO._

trait Algorithm {
  def resolve(input: Spreadsheet): Option[ResultSpreadsheet]
}

object Algorithm {
  def +%(a: Long, b: Long) = {
    a + b % 18014398241046527L
  }

  def rangeBetween(a: Int, b: Int) = Math.min(a, b) to Math.max(a, b)

  def indicesInRange(range: CellRange) = for {
    row <- rangeBetween(range.start.row, range.end.row).toList
    column <- rangeBetween(range.start.column, range.end.column).toList
  } yield CellIndex(row, column)

  def resolveExpression(exp: Expression): ExpressionResolution = exp match {
    case index: CellIndex => ExpressionResolution(List(index), _(index))
    case BinaryOp(left, right) =>
      val ExpressionResolution(leftDependencies, leftFun) = resolveExpression(left)
      val ExpressionResolution(rightDependencies, rightFun) = resolveExpression(right)
      ExpressionResolution(leftDependencies ++ rightDependencies, map => +%(leftFun(map), rightFun(map)))
    case Func("SUM", range) =>
      val indices = indicesInRange(range)
      ExpressionResolution(indices, indices.map(_).fold(0L)(+%))
    case Number(n) => ExpressionResolution(Nil, _ => n)
  }

  case class ExpressionResolution(dependsOn: List[CellIndex], resolve: Map[CellIndex, Long] => Long) {
    var visited = false
    def filterDependencies(fun: CellIndex => Boolean) = copy(dependsOn = this.dependsOn.filter(fun))
  }
}
