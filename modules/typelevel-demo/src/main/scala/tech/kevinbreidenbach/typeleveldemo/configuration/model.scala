package tech.kevinbreidenbach.typeleveldemo.configuration

import scala.concurrent.duration.FiniteDuration

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import natchez.Span
import org.http4s.Uri

type TracedIO[A] = Kleisli[IO, Span[IO], A]

opaque type DatadogAgentHost = Uri

object DatadogAgentHost {
  def apply(value: Uri): DatadogAgentHost = value

  extension (datadogAgentHost: DatadogAgentHost) {
    def value: Uri = datadogAgentHost
  }
}

opaque type DatadogEnabled = Boolean

object DatadogEnabled {
  def apply(value: Boolean): DatadogEnabled = value

  extension (datadogEnabled: DatadogEnabled) {
    def value: Boolean = datadogEnabled
  }
}

opaque type DatabaseUsername = String

object DatabaseUsername {
  def apply(value: String): DatabaseUsername = value

  extension (databaseUsername: DatabaseUsername) {
    def value: String = databaseUsername
  }
}

opaque type DatabasePassword = String

object DatabasePassword {
  def apply(value: String): DatabasePassword = value

  extension (databasePassword: DatabasePassword) {
    def value: String = databasePassword
  }
}

opaque type DatabaseHostname = String

object DatabaseHostname {
  def apply(value: String): DatabaseHostname = value

  extension (databaseHostname: DatabaseHostname) {
    def value: String = databaseHostname
  }

  given Show[DatabaseHostname] = _.value
}

opaque type DatabaseName = String

object DatabaseName {
  def apply(value: String): DatabaseName = value

  extension (databaseName: DatabaseName) {
    def value: String = databaseName
  }

  given Show[DatabaseName] = _.value
}

opaque type DatabaseSchema = String

object DatabaseSchema {
  def apply(value: String): DatabaseSchema = value

  extension (databaseSchema: DatabaseSchema) {
    def value: String = databaseSchema
  }

  given Show[DatabaseSchema] = _.value
}

opaque type DatabasePort = Int

object DatabasePort {
  def apply(value: Int): DatabasePort = value

  extension (databasePort: DatabasePort) {
    def value: Int = databasePort
  }

  given Show[DatabasePort] = _.value.toString
}

opaque type DatabaseThreadPoolSize = Int

object DatabaseThreadPoolSize {
  def apply(value: Int): DatabaseThreadPoolSize = value

  extension (databaseThreadPoolSize: DatabaseThreadPoolSize) {
    def value: Int = databaseThreadPoolSize
  }
}

opaque type DatabaseSocketTimeout = FiniteDuration

object DatabaseSocketTimeout {
  def apply(value: FiniteDuration): DatabaseSocketTimeout = value

  extension (databaseSocketTimeout: DatabaseSocketTimeout) {
    def value: FiniteDuration = databaseSocketTimeout
  }
}

opaque type DatabaseMigrationTimeout = FiniteDuration

object DatabaseMigrationTimeout {
  def apply(value: FiniteDuration): DatabaseMigrationTimeout = value

  extension (databaseMigrationTimeout: DatabaseMigrationTimeout) {
    def value: FiniteDuration = databaseMigrationTimeout
  }
}

opaque type RunDatabaseMigrations = Boolean

object RunDatabaseMigrations {
  def apply(value: Boolean): RunDatabaseMigrations = value

  extension (runDatabaseMigrations: RunDatabaseMigrations) {
    def value: Boolean = runDatabaseMigrations
  }
}

opaque type RetryTotal = Int

object RetryTotal {
  def apply(value: Int): RetryTotal = value

  extension (RetryTotal: RetryTotal) {
    def value: Int = RetryTotal
  }
}

opaque type RetryBackoff = FiniteDuration

object RetryBackoff {
  def apply(value: FiniteDuration): RetryBackoff = value

  extension (retryBackoff: RetryBackoff) {
    def value: FiniteDuration = retryBackoff
  }
}

opaque type HttpPort = Int

object HttpPort {
  def apply(value: Int): HttpPort = value

  extension (httpPort: HttpPort) {
    def value: Int = httpPort
  }
}

opaque type HttpHost = String

object HttpHost {
  def apply(value: String): HttpHost = value

  extension (httpHost: HttpHost) {
    def value: String = httpHost
  }
}

opaque type HttpIdleTimeout = FiniteDuration

object HttpIdleTimeout {
  def apply(value: FiniteDuration): HttpIdleTimeout = value

  extension (httpIdleTimeout: HttpIdleTimeout) {
    def value: FiniteDuration = httpIdleTimeout
  }
}

opaque type KafkaBootstrapServers = String

object KafkaBootstrapServers {
  def apply(value: String): KafkaBootstrapServers = value

  extension (kafkaBootstrapServers: KafkaBootstrapServers) {
    def value: String = kafkaBootstrapServers
  }
}

opaque type KafkaPeopleTopicName = String

object KafkaPeopleTopicName {
  def apply(value: String): KafkaPeopleTopicName = value

  extension (kafkaPeopleTopicName: KafkaPeopleTopicName) {
    def value: String = kafkaPeopleTopicName
  }
}

opaque type KafkaTransactionTopicName = String

object KafkaTransactionTopicName {
  def apply(value: String): KafkaTransactionTopicName = value

  extension (kafkaTransactionTopicName: KafkaTransactionTopicName) {
    def value: String = kafkaTransactionTopicName
  }
}

final case class KafkaConfig(
    bootstrapServers: KafkaBootstrapServers,
    peopleTopicName: KafkaPeopleTopicName,
    transactionTopicName: KafkaTransactionTopicName
)

final case class HttpServerConfig(port: HttpPort, host: HttpHost, idleTimeout: HttpIdleTimeout)

final case class RetryConfig(totalRetries: RetryTotal, retryBackoffDuration: RetryBackoff)

case class DatabaseConfig(
    username: DatabaseUsername,
    password: DatabasePassword,
    hostname: DatabaseHostname,
    database: DatabaseName,
    schema: DatabaseSchema,
    port: DatabasePort,
    threadPoolSize: DatabaseThreadPoolSize,
    socketTimeout: DatabaseSocketTimeout,
    migrationTimeout: DatabaseMigrationTimeout,
    runDatabaseMigrations: RunDatabaseMigrations
)

final case class DatadogConfig(agentHost: DatadogAgentHost, enable: DatadogEnabled)

final case class AllConfig(
    httpServerConfig: HttpServerConfig,
    producerConfig: KafkaConfig,
    databaseConfig: DatabaseConfig,
    retryConfig: RetryConfig,
    datadogConfig: DatadogConfig
)

final case class ConfigError(message: String) extends Exception(message)
