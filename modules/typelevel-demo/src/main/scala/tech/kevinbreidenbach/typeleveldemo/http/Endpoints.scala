package tech.kevinbreidenbach.typeleveldemo.http

import tech.kevinbreidenbach.typeleveldemo.health.HealthStatus
import tech.kevinbreidenbach.typeleveldemo.health.Status.Healthy
import tech.kevinbreidenbach.typeleveldemo.health.Status.Running
import tech.kevinbreidenbach.typeleveldemo.health.Status.Unhealthy
import tech.kevinbreidenbach.typeleveldemo.json.given

import sttp.model.StatusCode.InternalServerError
import sttp.model.StatusCode.Ok
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody

type HealthCheckEndpoint = PublicEndpoint[Unit, HealthStatus, HealthStatus, Any]

trait Endpoints {
  def healthCheck: HealthCheckEndpoint
}

object Endpoints {
  def make: Endpoints = new EndpointsImpl
}

class EndpointsImpl private[http] extends Endpoints {
  override def healthCheck: HealthCheckEndpoint =
    endpoint.get
      .in("healthcheck")
      .out {
        jsonBody[HealthStatus]
          .example(HealthStatus(Running, Healthy, Healthy, Healthy, Healthy))
          .description("The health of the system")
      }
      .out(statusCode(Ok))
      .errorOut {
        jsonBody[HealthStatus]
          .example(HealthStatus(Unhealthy, Unhealthy, Healthy, Healthy, Healthy))
          .description("The health of the system if unhealthy")
      }
      .errorOut(statusCode(InternalServerError))
      .description("Health Check for the System")
}
