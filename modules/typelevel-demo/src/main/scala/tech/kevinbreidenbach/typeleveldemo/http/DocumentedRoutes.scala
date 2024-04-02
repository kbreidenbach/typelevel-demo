package tech.kevinbreidenbach.typeleveldemo.http

import tech.kevinbreidenbach.typeleveldemo.domain.SpanName
import tech.kevinbreidenbach.typeleveldemo.health.Health
import tech.kevinbreidenbach.typeleveldemo.health.HealthStatus
import tech.kevinbreidenbach.typeleveldemo.util.LogLevel
import tech.kevinbreidenbach.typeleveldemo.util.appName
import tech.kevinbreidenbach.typeleveldemo.util.appVersion

import cats.effect.Async
import cats.syntax.all.given
import natchez.Trace
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.StructuredLogger
import sttp.apispec.openapi.Info
import sttp.apispec.openapi.circe.yaml.given
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

trait DocumentedRoutes[F[_]] extends Http4sDsl[F] {
  val routes: HttpRoutes[F]
}

object DocumentedRoutes {
  def make[F[_]: Async: StructuredLogger: Trace](endpoints: Endpoints, health: Health[F]): DocumentedRoutes[F] =
    new Routes[F](endpoints, health)
}

class Routes[F[_]: Async: StructuredLogger: Trace](endpoints: Endpoints, health: Health[F])
    extends DocumentedRoutes[F] {

  given SpanName = SpanName("http-endpoints")

  private val info = Info(appName.show, appVersion.show)

  private val healthCheck = endpoints.healthCheck.serverLogic { _ =>
    health.currentHealth.flatMap {
      case h if h.isHealthy => h.asRight[HealthStatus].pure[F]
      case h                => LogLevel.Error[F, HealthStatus](h.show, h.some).log >> h.asLeft[HealthStatus].pure[F]
    }
  }

  private val allDocumentedRoutes = List(healthCheck)

  private val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(allDocumentedRoutes, info)

  private val serverInterpreter = Http4sServerInterpreter[F]()

  override val routes: HttpRoutes[F] =
    serverInterpreter.toRoutes(SwaggerUI[F](openApi.toYaml)) <+> serverInterpreter.toRoutes(allDocumentedRoutes)
}
