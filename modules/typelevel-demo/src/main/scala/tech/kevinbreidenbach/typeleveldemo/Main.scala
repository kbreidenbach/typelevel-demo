package tech.kevinbreidenbach.typeleveldemo

import tech.kevinbreidenbach.typeleveldemo.configuration.AllConfig
import tech.kevinbreidenbach.typeleveldemo.configuration.Config
import tech.kevinbreidenbach.typeleveldemo.configuration.TracedIO
import tech.kevinbreidenbach.typeleveldemo.domain.SpanName
import tech.kevinbreidenbach.typeleveldemo.domain.mainSpanName
import tech.kevinbreidenbach.typeleveldemo.health.Health
import tech.kevinbreidenbach.typeleveldemo.health.Status.Running
import tech.kevinbreidenbach.typeleveldemo.health.Status.ShuttingDown
import tech.kevinbreidenbach.typeleveldemo.health.Status.StartingUp
import tech.kevinbreidenbach.typeleveldemo.persistence.Persistence
import tech.kevinbreidenbach.typeleveldemo.resources.Resources
import tech.kevinbreidenbach.typeleveldemo.util.LogLevel
import tech.kevinbreidenbach.typeleveldemo.util.RunWithRetry
import tech.kevinbreidenbach.typeleveldemo.util.buildInfoContext

import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.all.given
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import com.ovoenergy.natchez.extras.log4cats.TracedLogger
import fs2.Stream
import fs2.io.net.Network
import natchez.Trace
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main
    extends CommandIOApp(
      "typelevel-demo",
      "A demo for typelevel stack (cats-effect 3), and Scala 3",
      true,
      BuildInfo.version
    ) {
  private given SelfAwareStructuredLogger[TracedIO] =
    TracedLogger.lift[IO](Slf4jLogger.getLoggerFromClass[IO](this.getClass).addContext(buildInfoContext))

  private given SpanName = mainSpanName

  override def main: Opts[IO[ExitCode]] =
    Config.applicationConfig.map { conf =>
      val appResource = for {
        span <- Resources.makeDatadogEntryPoint[IO](conf)
        exitCode = runApp(conf).run(span) // the run(span) causes the TracedIO context to run and collapse back to IO
        _        = traceAndLog("People Service Stopped")
      } yield exitCode

      appResource.use(identity)
    }

  // This sets up to run the application in the TracedIO context
  private def runApp(appConfig: AllConfig)(using StructuredLogger[TracedIO]): TracedIO[ExitCode] =
    app[TracedIO](appConfig)
      .use(identity)
      .handleErrorWith(err =>
        LogLevel
          .Error(s"Fatal Error: halting people service: ${err.getMessage}", error = Some(err))
          .log
          .map(_ => ExitCode.Error)
      )

  private def app[F[_]: Async: Network: StructuredLogger: Trace](appConfig: AllConfig): Resource[F, F[ExitCode]] =
    for {
      _         <- Resource.eval(traceAndLog("People Service Starting..."))
      health    <- Resource.eval(Health.make[F])
      _         <- Resource.eval(health.updateSystemStatus(StartingUp))
      resources <- Resources.make[F](appConfig)
      given RunWithRetry[F] = RunWithRetry.make[F](appConfig.retryConfig)
      persistence           = Persistence.make(resources.transactor)
      exitCode <-
        Resource.eval(
          Stream
            .eval(health.updateSystemStatus(Running))
            .compile
            .drain
            .flatMap { _ =>
              for {
                _ <- health.updateSystemStatus(ShuttingDown)
                _ <- traceAndLog("Health Check Stopped")
              } yield ExitCode.Success
            }
        )
    } yield for {
      _ <- traceAndLog("People Service Started")
    } yield exitCode

  private def traceAndLog[F[_]: StructuredLogger: Trace](msg: String): F[Unit] = LogLevel.Info[F, Unit](msg).log
}
