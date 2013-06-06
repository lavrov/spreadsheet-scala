package spreadsheet.algorithm

import scalaz._
import spreadsheet.IO._
import spreadsheet.Model._
import std.option._
import std.list._
import syntax.std.boolean._
import syntax.traverse._
import syntax.apply._
import scala.Some

object DepthFirstSingleThread extends Algorithm {
  import Algorithm._

  def resolve(spreadsheet: Spreadsheet) = {
    val map = collection.mutable.Map.empty[CellIndex, Long]

    def resolve(index: CellIndex, fringe: Set[CellIndex]): Option[Long] = {
      lazy val deeperFringe = fringe + index

      def resolveExpression(expr: Expression): Option[Long] = expr match {
        case Number(num) => Some(num)
        case BinaryOp(left, right) => ^(resolveExpression(left), resolveExpression(right))(+%)
        case Func("SUM", range) => resolveRange(range).map(_.foldLeft(0L)(+%))
        case ind: CellIndex => resolve(ind, deeperFringe)
        case _ => None
      }

      def resolveRange(range: CellRange) = Applicative[Option].sequence {
          indicesInRange(range).map(resolve(_, deeperFringe))
      }

      fringe.contains(index).fold(
        None,
        for {
          result <- map.get(index).orElse {
            val exp = spreadsheet.get(index)
            if(exp.isDefined)
              exp.flatMap ( e => resolveExpression(e).map { r => map.update(index, r); r })
            else
              Some(0L)
          }
        } yield result
      )
    }

    spreadsheet.keys.toList.map(index => resolve(index, Set.empty[CellIndex])).sequence.map { _ =>
      map.toMap
    }
  }

}