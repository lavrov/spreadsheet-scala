package spreadsheet.algorithm.parallel

import spreadsheet._
import algorithm.Algorithm
import Model._
import IO._
import scalaz._
import std.option._
import std.list._
import std.vector._
import syntax.traverse._
import syntax.std.boolean._
import concurrent._
import duration._
import ExecutionContext.Implicits._
import akka.agent._

object ParallelAlgorithm extends Algorithm {
  import Algorithm._

  def resolve(input: Spreadsheet): Option[ResultSpreadsheet] = {

    val spreadsheetResolution = input.map { case (index, expression) =>
      index -> resolveExpression(expression)
    }

    val freeCells = collection.mutable.ListBuffer.empty[CellIndex]

    def depthFirstSearch(fringe: Set[CellIndex])(index: CellIndex): Option[Vector[(CellIndex, CellIndex)]] = {
      fringe.contains(index).fold(None,  {
        val node = spreadsheetResolution(index)
        node.visited.fold(Some(Vector.empty), {
          val dependencies = node.dependsOn.toVector
          node.visited = true
          if(dependencies.isEmpty) freeCells += index
          dependencies.map(depthFirstSearch(fringe + index)).sequence.map(_.flatten).map(dependencies.map(_ -> index) ++ _)
       })
      })
    }

    val dependentMapOption = spreadsheetResolution.keys.toVector.map(depthFirstSearch(Set.empty)).sequence.map(_.flatten).map(list =>
      list.groupBy(_._1).mapValues(_.map(_._2))
    )

    dependentMapOption.map { dependentMap =>

      implicit val system = akka.actor.ActorSystem.create("default")

      val agents =
        spreadsheetResolution.map { case (index, resolution) =>
          index -> Agent {
            CellResolver(index, resolution.resolve, resolution.dependsOn.toSet)
          }
        }

      def notifyAgents(index: CellIndex, value: Long) {
        dependentMap.get(index).foreach { dep =>
          dep.map(agents).foreach(_.send(_.resolve(index, value)))
        }
      }


      freeCells.map(agents).foreach(_.send(_.tryResolve))

      val futures = agents.map { case (index, agent) =>
        agent.get().future.map { result =>
          notifyAgents(index, result)
          index -> result
        }
      }.toList

      try Await.result(Future.sequence(futures), 10.second).toMap finally { system.shutdown() }
    }
  }



  case class CellResolver(index: CellIndex,
                          resolveFun: Map[CellIndex, Long] => Long,
                          awaiting: Set[CellIndex],
                          resolved: Map[CellIndex, Long] = Map.empty,
                          promise: Promise[Long] = Promise[Long]()) {

    def future = promise.future

    def tryResolve = {
      (awaiting.isEmpty).option {
        promise.success(resolveFun(resolved.withDefaultValue(0L)))
      }
      this
    }

    def resolve(index: CellIndex, value: Long) = {
      copy(awaiting = this.awaiting - index, resolved = resolved + (index -> value)).tryResolve
    }
  }
}