package spreadsheet

import io._
import scalaz._
import std.option._
import std.list._
import syntax.std.boolean._
import syntax.traverse._

object Resolver {
  def resolve(input: Input) = {
    MapSpreadsheetResolver(input).resolve
  }
}

trait SpreadsheetResolver {
  def resolve: Option[Seq[(CellIndex, Cell)]]
}

case class MapSpreadsheetResolver(input: Input) extends SpreadsheetResolver {
  private val map =
    collection.mutable.Map[CellIndex, Cell](input.map(c => c.index -> UnresolvedCell(c.expression)): _*)

  def resolve = {
    map.keys.toList.map(index => resolve(index, Set.empty[CellIndex])).sequence.map { _ =>
      map.toSeq.sortBy {
        case (index, _) => index.row -> index.column
      }
    }
  }

  def resolve(index: CellIndex, fringe: Set[CellIndex]): Option[Int] = {
    lazy val deeperFringe = fringe + index
    def resolveExpression(expr: Expression): Option[Int] = expr match {
      case Number(num) => Some(num)
      case BinaryOp(left, right) => Applicative[Option].apply2(resolveExpression(left), resolveExpression(right))(_ + _)
      case Func("SUM", range) => resolveRange(range).map(_.sum)
      case ind: CellIndex => resolve(ind, deeperFringe)
      case _ => None
    }

    def resolveRange(range: CellRange): Option[List[Int]] =
      (
        for {
          row <- Math.min(range.start.row, range.end.row) to Math.max(range.start.row, range.end.row)
          column <- Math.min(range.start.column, range.end.column) to Math.max(range.start.column, range.end.column)
          ind = CellIndex(row, column)
        } yield resolve(ind, deeperFringe)
      ).toList.sequence

    fringe.contains(index).fold(
      None,
      map.getOrElse(index, ResolvedCell(0)) match {
        case ResolvedCell(result) => Some(result)
        case UnresolvedCell(expr) =>
          resolveExpression(expr).map { number =>
            map.update(index, ResolvedCell(number))
            number
          }
      }
    )
  }
}

trait Cell
case class UnresolvedCell(expression: Expression) extends Cell
case class ResolvedCell(result: Int) extends Cell