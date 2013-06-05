package spreadsheet

import IO._
import Model._
import scalaz._
import std.option._
import std.list._
import syntax.std.boolean._
import syntax.traverse._
import syntax.apply._

object Resolver {
  private def +%(a: Long, b: Long) = a + b % 18014398241046527L

  def resolve(input: Input) = {
    val map =
      collection.mutable.Map[CellIndex, Cell](input.map(c => c.index -> UnresolvedCell(c.expression)): _*)

    def resolve(index: CellIndex, fringe: Set[CellIndex]): Option[Long] = {
      lazy val deeperFringe = fringe + index

      def resolveExpression(expr: Expression): Option[Long] = expr match {
        case Number(num) => Some(num)
        case BinaryOp(left, right) => ^(resolveExpression(left), resolveExpression(right))(+%)
        case Func("SUM", range) => resolveRange(range).map(_.foldLeft(0L)(+%))
        case ind: CellIndex => resolve(ind, deeperFringe)
        case _ => None
      }

      def rangeBetween(a: Int, b: Int) = Math.min(a, b) to Math.max(a, b)

      def resolveRange(range: CellRange) = Applicative[Option].sequence {
          for {
            row <- rangeBetween(range.start.row, range.end.row).toList
            column <- rangeBetween(range.start.column, range.end.column).toList
          } yield resolve(CellIndex(row, column), deeperFringe)
      }

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

    map.keys.toList.map(index => resolve(index, Set.empty[CellIndex])).sequence.map { _ =>
      map.toSeq.sortBy {
        case (index, _) => index.row -> index.column
      }
    }
  }

}