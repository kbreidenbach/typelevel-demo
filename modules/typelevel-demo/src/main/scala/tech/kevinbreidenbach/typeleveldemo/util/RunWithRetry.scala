package tech.kevinbreidenbach.typeleveldemo.util

import tech.kevinbreidenbach.typeleveldemo.configuration.RetryConfig
import tech.kevinbreidenbach.typeleveldemo.domain.SpanName

import cats.effect.Temporal
import cats.syntax.all.given
import natchez.Trace
import org.typelevel.log4cats.StructuredLogger
import retry.RetryDetails
import retry.RetryDetails.GivingUp
import retry.RetryDetails.WillDelayAndRetry
import retry.RetryPolicies.exponentialBackoff
import retry.RetryPolicies.limitRetries
import retry.RetryPolicy
import retry.retryingOnAllErrors

trait RunWithRetry[F[_]] {
  def retry[A](retryMessage: String)(closure: F[A])(using span: SpanName): F[A]
}

object RunWithRetry {
  def make[F[_]: StructuredLogger: Trace: Temporal](retryConfig: RetryConfig): RunWithRetry[F] =
    new RunWithRetryImpl[F](retryConfig)

  def apply[F[_]](using ev: RunWithRetry[F]): RunWithRetry[F] = ev
}

private class RunWithRetryImpl[F[_]: StructuredLogger: Trace: Temporal](retryConfig: RetryConfig)
    extends RunWithRetry[F] {
  private val retryPolicy: RetryPolicy[F] =
    limitRetries[F](retryConfig.totalRetries.value) |+| exponentialBackoff[F](retryConfig.retryBackoffDuration.value)

  override def retry[A](retryMessage: String)(res: F[A])(using span: SpanName): F[A] =
    retryingOnAllErrors[A](
      policy = retryPolicy,
      onError = logRetry(retryMessage)
    )(res)

  private def logRetry(retryMessage: String)(e: Throwable, details: RetryDetails)(using span: SpanName) =
    details match {
      case retryDetails: WillDelayAndRetry =>
        LogLevel
          .Warn[F, Unit](
            s"$retryMessage failed with error $e, have conducted ${retryDetails.retriesSoFar} retries so far",
            error = Some(e)
          )
          .log
      case g: GivingUp                     =>
        LogLevel
          .Error[F, Unit](
            s"$retryMessage failed with error $e. Giving up after ${g.totalRetries} retries",
            error = Some(e)
          )
          .log
    }
}
