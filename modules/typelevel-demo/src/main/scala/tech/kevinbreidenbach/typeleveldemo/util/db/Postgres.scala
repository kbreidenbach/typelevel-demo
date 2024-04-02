package tech.kevinbreidenbach.typeleveldemo.util.db

import tech.kevinbreidenbach.typeleveldemo.configuration.*

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.given
import com.zaxxer.hikari.HikariConfig
import doobie.LogHandler
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.typelevel.log4cats.StructuredLogger

trait Postgres[F[_]] {
  val transactor: Resource[F, Transactor[F]]
  def runMigrations(): F[Unit]
}

object Postgres {
  def make[F[_]: Async: StructuredLogger](config: DatabaseConfig): PostgresImpl[F] =
    new PostgresImpl[F](config, DatabaseLogHandler.make[F])
}

case object MigrationTimeoutError extends Exception(s"Database migration timeout")

class PostgresImpl[F[_]: Async] private[db] (config: DatabaseConfig, logHandler: LogHandler[F]) extends Postgres[F] {
  private val jdbcUrl =
    show"jdbc:postgresql://${config.hostname}:${config.port}/${config.database}?currentSchema=${config.schema}"

  private val hikariConfig: HikariConfig = {
    val hConfig = new HikariConfig()

    hConfig.setJdbcUrl(jdbcUrl)
    hConfig.setUsername(config.username.value)
    hConfig.setPassword(config.password.value)
    hConfig.setDriverClassName("org.postgresql.Driver")
    hConfig.setMaximumPoolSize(config.threadPoolSize.value)
    hConfig.setAutoCommit(false)
    hConfig.addDataSourceProperty("socketTimeout", config.socketTimeout.value.toSeconds.toString)

    hConfig
  }

  private def migrate: F[MigrateResult] =
    Async[F].delay {
      Flyway
        .configure()
        .dataSource(hikariConfig.getDataSource)
        .locations("classpath:sql")
        .baselineOnMigrate(true)
        .loggers("slf4j")
        .load()
        .migrate()
    }

  override val transactor: Resource[F, Transactor[F]] =
    for {
      connectionExecutionContext <- ExecutionContexts.fixedThreadPool[F](config.threadPoolSize.value)
      transactionExecutor        <-
        HikariTransactor.fromHikariConfigCustomEc(hikariConfig, connectionExecutionContext, Some(logHandler))
    } yield transactionExecutor

  override def runMigrations(): F[Unit] =
    if (config.runDatabaseMigrations.value) {
      Async[F].timeoutTo(
        migrate.void,
        config.migrationTimeout.value,
        Async[F].raiseError(MigrationTimeoutError)
      )
    } else Async[F].unit
}
