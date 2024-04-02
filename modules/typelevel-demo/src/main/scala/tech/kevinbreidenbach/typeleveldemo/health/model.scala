package tech.kevinbreidenbach.typeleveldemo.health

import tech.kevinbreidenbach.typeleveldemo.util.LogContext

import cats.Show
import cats.syntax.show.given

enum Status {
  case StartingUp
  case Running
  case ShuttingDown
  case Healthy
  case Unhealthy
}

object Status {
  val healthyStatuses: Set[Status] = Set(StartingUp, Running, Healthy)
}

given Show[Status] = _.toString

case class HealthStatus(
    systemStatus: Status,
    databaseStatus: Status,
    kafkaConsumerStatus: Status,
    kafkaProducerStatus: Status,
    previousSystemStatus: Status = Status.Healthy
) {
  private val statuses   = List(systemStatus, databaseStatus, kafkaConsumerStatus, kafkaProducerStatus)
  val isHealthy: Boolean = statuses.forall(Status.healthyStatuses.contains)
}

object HealthStatus {
  given Show[HealthStatus] =
    Show.show { status =>
      s"""Status:
         |\tSystem: ${status.systemStatus}
         |\tDatabase: ${status.databaseStatus}
         |\tKafka Consumer: ${status.kafkaConsumerStatus}
         |\tKafka Producer: ${status.kafkaProducerStatus}""".stripMargin
    }

  given LogContext[HealthStatus] with
    extension (h: HealthStatus) {
      def logContext: Map[String, String] =
        Map(
          "system-status"         -> h.systemStatus.show,
          "database-status"       -> h.databaseStatus.show,
          "kafka-consumer-status" -> h.kafkaConsumerStatus.show,
          "kafka-producer-status" -> h.kafkaProducerStatus.show
        )
    }
}
