package com.github.kabdelrahman.transtat.metrics


import akka.http.scaladsl.server.{RequestContext, _}
import com.codahale.metrics.MetricRegistry
import nl.grons.metrics.scala._

// TODO Enable endpoint for metrics in running the application and deployments
// TODO Experiment and compare Scalatra vs Codahale DropWizard
class Metrics(val metricRegistry: MetricRegistry) extends InstrumentedBuilder {
  override lazy val metricBaseName = MetricName("transtat.response")
  val transactionMetrics: Timer = metrics.timer("transaction-post-response")
  val statsMetrics: Timer = metrics.timer("stats-get-response")
}

object AppWideMetrics extends Metrics(new com.codahale.metrics.MetricRegistry())

trait TimerSupport {
  def timeit(timer: Timer)(route: => Route): Route = { ctx: RequestContext =>
    import scala.concurrent.ExecutionContext.Implicits.global
    timer.timeFuture(route(ctx))
  }
}

object TimerSupport extends TimerSupport

