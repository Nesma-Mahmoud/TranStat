package com.github.kabdelrahman.transtat.service.transaction

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern._
import akka.util.Timeout
import com.github.kabdelrahman.transtat.bootstrap.AppConfig
import com.github.kabdelrahman.transtat.codecs.DefaultJsonFormats
import com.github.kabdelrahman.transtat.metrics.Metrics
import com.github.kabdelrahman.transtat.metrics.TimerSupport._
import com.github.kabdelrahman.transtat.model.Transaction
import com.github.kabdelrahman.transtat.service.Cache
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


@Api(
  value = "/transactions",
  description = "Transaction service which is called every time a transaction is made. " +
    "It is also the sole input of this rest API",
  produces = "application/json")
@Path("/transactions")
class TransactionService()(implicit executionContext: ExecutionContext,
                           implicit val metrics: Metrics,
                           implicit val system: ActorSystem,
                           implicit val cache: Cache[Transaction])
  extends Directives with DefaultJsonFormats with AppConfig {

  // That's a very high timeout that should be monitored by metrics and reduced overtime.
  implicit val timeout = Timeout(2.seconds)

  implicit val transactionModelFormat: RootJsonFormat[Transaction] = jsonFormat2(Transaction)

  val route: Route = postTransaction

  @ApiOperation(
    value =
      "Return either " +
        "`201` if transaction posted successfully or " +
        "`204` if transaction is older than 60 seconds",
    httpMethod = "POST")
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Return `Ok` for successful response"),
    new ApiResponse(code = 204, message = "Return `No Content` If transaction is older than 60 seconds")
  ))
  def postTransaction: Route =
    path("transactions") {
      post {
        entity(as[Transaction]) { transaction =>
          timeit(metrics.transactionMetrics) {
            complete {
              (system.actorOf(TransactionActor(cache)) ? transaction).mapTo[TransactionOpResponse]
                .map(resp => if (resp.successful) {
                  StatusCodes.OK
                } else {
                  StatusCodes.NoContent
                }
                )
            }
          }
        }
      }
    }
}






