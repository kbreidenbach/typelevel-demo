package tech.kevinbreidenbach.typeleveldemo.resources

import tech.kevinbreidenbach.typeleveldemo.BuildInfo
import tech.kevinbreidenbach.typeleveldemo.configuration.AllConfig
import tech.kevinbreidenbach.typeleveldemo.configuration.DatabaseConfig
import tech.kevinbreidenbach.typeleveldemo.domain.mainSpanName
import tech.kevinbreidenbach.typeleveldemo.util.buildInfoContext
import tech.kevinbreidenbach.typeleveldemo.util.db.Postgres

import cats.effect.Async
import cats.effect.Resource
import com.ovoenergy.natchez.extras.datadog.Datadog
import doobie.Transactor
import fs2.io.net.Network
import natchez.Span
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final case class Resources[F[_]](
    transactor: Transactor[F]
)

object Resources {
  def make[F[_]: Async](cfg: AllConfig): Resource[F, Resources[F]] = {

    def transactor(config: DatabaseConfig): Resource[F, Transactor[F]] = {
      given StructuredLogger[F] = Slf4jLogger.getLoggerFromName[F]("DBLogger").addContext(buildInfoContext)
      Postgres.make(config).transactor
    }

    transactor(cfg.databaseConfig).map(Resources.apply[F])
  }

  def makeDatadogEntryPoint[F[_]: Async: Network](appConfig: AllConfig): Resource[F, Span[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].build
      entryPoint <- Datadog.entryPoint(httpClient, BuildInfo.name, "default", appConfig.datadogConfig.agentHost.value)
      span       <- if (appConfig.datadogConfig.enable.value) entryPoint.root(mainSpanName.value)
                    else Resource.pure[F, Span[F]](Span.noop[F])
    } yield span
}
