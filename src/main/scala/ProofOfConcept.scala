import concurrent._
import duration._
import java.util.concurrent.ForkJoinPool

object ProofOfConcept /*extends App*/ {
  val pool = new ForkJoinPool(3)

  implicit val ctx = ExecutionContext.fromExecutorService(pool)

  val p1 = Promise[Int]()
  val f1 = p1.future
  val p2 = Promise[Int]()
  val f2 = p2.future
  val p3 = Promise[Int]()
  val f3 = p3.future

  Future.successful()

  val firstCell = future {
    val r1 = Await.result(f1, Duration.Inf)
    val r2 = Await.result(f2, Duration.Inf)
    r1 + r2
  }

  val secondCell = future {
    val r2 = Await.result(f2, Duration.Inf)
    val r3 = Await.result(f3, Duration.Inf)

    r2 + r3
  }

  val mainFuture = for (first <- firstCell; second <- secondCell) yield first + second

  println("Running...")

  future(
    p1 success 5
  )

  future (
    p2 success 10
  )

  future (
    p3 success 15
  )

  val result = Await.result(mainFuture, 5.second)

  val result1 = Await.result(mainFuture, 5.second)


  println(s"Here is result: $result and $result1")
}
