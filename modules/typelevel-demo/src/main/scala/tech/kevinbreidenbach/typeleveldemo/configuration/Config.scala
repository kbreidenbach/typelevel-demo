package tech.kevinbreidenbach.typeleveldemo.configuration

import scala.concurrent.duration.*

import cats.syntax.all.given
import com.monovore.decline.Opts
import org.http4s.Uri

object Config {
  private val databaseConfig: Opts[DatabaseConfig] = (
    Opts.env[String]("POSTGRES_USER", "Postgres username").map(DatabaseUsername.apply),
    Opts.env[String]("POSTGRES_PASSWORD", "Postgres password").map(DatabasePassword.apply),
    Opts
      .env[String]("POSTGRES_HOSTNAME", "Postgres hostname (default: localhost)")
      .withDefault("localhost")
      .map(DatabaseHostname.apply),
    Opts
      .env[String]("POSTGRES_DATABASE", "Postgres database (default: typelevel-demo)")
      .withDefault("typelevel-demo")
      .map(DatabaseName.apply),
    Opts
      .env[String]("POSTGRES_SCHEMA", "Postgres schema (default: public)")
      .withDefault("public")
      .map(DatabaseSchema.apply),
    Opts.env[Int]("POSTGRES_PORT", "Postgres port (default: 5432)").withDefault(5432).map(DatabasePort.apply),
    Opts
      .env[Int]("POSTGRES_THREAD_POOL_SIZE", "Hikari thread pool size (default: 10)")
      .withDefault(10)
      .map(DatabaseThreadPoolSize.apply),
    Opts
      .env[FiniteDuration]("POSTGRES_SOCKET_TIMEOUT", "Postgres connection socket timeout (default: 1 minute")
      .withDefault(1.minute)
      .map(DatabaseSocketTimeout.apply),
    Opts
      .env[FiniteDuration]("POSTGRES_MIGRATION_TIMEOUT", "Migration timeout (default: 1 minute")
      .withDefault(1.minute)
      .map(DatabaseMigrationTimeout.apply),
    Opts
      .env[String]("RUN_DATABASE_MIGRATIONS", "Run the database migrations on start up (default: true)")
      .withDefault("True")
      .map(_.toBoolean)
      .map(RunDatabaseMigrations.apply)
  ).mapN(DatabaseConfig.apply)

  private val httpServerConfig: Opts[HttpServerConfig] = (
    Opts.env[Int]("SERVER_PORT", "The port to bind to (default: 8080)").withDefault(8080).map(HttpPort.apply),
    Opts
      .env[String]("SERVER_HOST", "The host to bind to (default: 0.0.0.0)")
      .withDefault("0.0.0.0")
      .map(HttpHost.apply),
    Opts
      .env[FiniteDuration]("SERVER_IDLE_TIMEOUT", "Time a connection can remain idle (default: 60 seconds)")
      .withDefault(60.seconds)
      .map(HttpIdleTimeout.apply)
  ).mapN(HttpServerConfig.apply)

  private val retryConfig: Opts[RetryConfig] = (
    Opts
      .env[Int]("RETRY_ATTEMPTS", "The maximum retry attempts before failure (default: 5)")
      .withDefault(5)
      .map(RetryTotal.apply),
    Opts
      .env[FiniteDuration]("RETRY_BACKOFF_DURATION", "The backoff retry time (default: 500 milliseconds)")
      .withDefault(500.milliseconds)
      .map(RetryBackoff.apply)
  ).mapN(RetryConfig.apply)

  private val kafkaConfig: Opts[KafkaConfig] = (
    Opts
      .env[String]("KAFKA_BOOTSTRAP_SERVERS", "The Kafka bootstrap servers (default: 127.0.0.1:9091)")
      .withDefault("127.0.0.1:9091")
      .map(KafkaBootstrapServers.apply),
    Opts
      .env[String]("KAFKA_PEOPLE_TOPIC", "The topic to publish people on (default: people.json)")
      .withDefault("people.json")
      .map(KafkaPeopleTopicName.apply),
    Opts
      .env[String]("KAFKA_TRANSACTION_TOPIC", "The topic consume transactions on (default: transaction.json)")
      .withDefault("transaction.json")
      .map(KafkaTransactionTopicName.apply)
  ).mapN(KafkaConfig.apply)

  private val datadogConfig: Opts[DatadogConfig] = (
    Opts
      .env[String]("DD_AGENT_HOST", "The Datadog agent host")
      .map(Uri.fromString)
      .map {
        case Left(e)  => throw ConfigError(s"Invalid Datadog agent host: ${e.getMessage}")
        case Right(u) => u
      }
      .map(DatadogAgentHost.apply),
    Opts
      .env[String]("DD_TRACE_ENABLED", "Should trace data be sent to datadog (default: true)")
      .withDefault("True")
      .map(_.toBoolean)
      .map(DatadogEnabled.apply)
  ).mapN(DatadogConfig.apply)

  val applicationConfig: Opts[AllConfig] =
    (httpServerConfig, kafkaConfig, databaseConfig, retryConfig, datadogConfig).mapN(AllConfig.apply)
}
