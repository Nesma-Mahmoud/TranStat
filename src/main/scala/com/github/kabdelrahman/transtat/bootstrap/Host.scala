package com.github.kabdelrahman.transtat.bootstrap

import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.kabdelrahman.transtat.api.{Api, Core}
import com.github.kabdelrahman.transtat.bootstrap.AppConfig._
import com.typesafe.scalalogging.LazyLogging

trait Host {
  this: Api with Core with LazyLogging =>
  implicit val mat = ActorMaterializer()
  Http().bindAndHandle(routes, httpHost, httpPort)
  logger.info(s"application started on $httpHost:$httpPort")
}
