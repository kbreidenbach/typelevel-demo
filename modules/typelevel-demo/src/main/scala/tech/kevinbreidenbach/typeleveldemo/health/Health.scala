package tech.kevinbreidenbach.typeleveldemo.health

import tech.kevinbreidenbach.typeleveldemo.domain.SpanName
import tech.kevinbreidenbach.typeleveldemo.health.Status.Healthy
import tech.kevinbreidenbach.typeleveldemo.health.Status.StartingUp
import tech.kevinbreidenbach.typeleveldemo.health.Status.Unhealthy
import tech.kevinbreidenbach.typeleveldemo.util.LogLevel

import cats.effect.Ref
import cats.effect.kernel.Concurrent
import cats.syntax.all.given
import natchez.Trace
import org.typelevel.log4cats.StructuredLogger

trait Health[F[_]] {
  def currentHealth: F[HealthStatus]
  def updateSystemStatus(status: Status): F[HealthStatus]
  def updateDatabaseStatus(status: Status): F[HealthStatus]
  def updateKafkaConsumerStatus(status: Status): F[HealthStatus]
  def updateKafkaProducerStatus(status: Status): F[HealthStatus]
}

object Health {
  def make[F[_]: Concurrent: StructuredLogger: Trace]: F[Health[F]] =
    Ref.of[F, HealthStatus](HealthStatus(StartingUp, Healthy, Healthy, Healthy)).map(r => new HealthImpl[F](r))
}

class HealthImpl[F[_]: Concurrent: StructuredLogger: Trace] private[health] (currentStatus: Ref[F, HealthStatus])
    extends Health[F] {

  private given SpanName = SpanName("health-check")

  override def currentHealth: F[HealthStatus] = currentStatus.get

  override def updateSystemStatus(status: Status): F[HealthStatus] =
    for {
      current   <- currentStatus.get
      newStatus <- currentStatus.updateAndGet(_.copy(systemStatus = status))
      _         <- (status match {
                     case Unhealthy                                            =>
                       LogLevel.Error[F, HealthStatus](_, newStatus.some)
                     case Healthy if current.previousSystemStatus == Unhealthy =>
                       LogLevel.Info[F, HealthStatus](_, newStatus.some)
                     case _                                                    =>
                       LogLevel.Debug[F, HealthStatus](_, newStatus.some)
                   })(s"system status updated to $status").log
    } yield newStatus

  override def updateDatabaseStatus(status: Status): F[HealthStatus] =
    currentStatus.updateAndGet(_.copy(databaseStatus = status)) >> checkSystemStatus

  override def updateKafkaConsumerStatus(status: Status): F[HealthStatus] =
    currentStatus.updateAndGet(_.copy(kafkaConsumerStatus = status)) >> checkSystemStatus

  override def updateKafkaProducerStatus(status: Status): F[HealthStatus] =
    currentStatus.updateAndGet(_.copy(kafkaProducerStatus = status)) >> checkSystemStatus

  private def updatePreviousStatus(status: Status): F[HealthStatus] =
    currentStatus.updateAndGet(_.copy(previousSystemStatus = status))

  private def checkSystemStatus: F[HealthStatus] =
    currentStatus.get.flatTap { status =>
      updatePreviousStatus(status.systemStatus) >> {
        if (status.isHealthy)
          updateSystemStatus(Healthy)
        else
          updateSystemStatus(Unhealthy)
      }
    }
}
