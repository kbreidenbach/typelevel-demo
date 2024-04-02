package tech.kevinbreidenbach.typeleveldemo.util

import tech.kevinbreidenbach.typeleveldemo.domain.SpanName

import cats.syntax.show.given
import natchez.Trace
import org.typelevel.log4cats.StructuredLogger

enum LogLevel[G[_], L: LogContext](
    message: String,
    ctx: Option[L] = None,
    error: Option[Throwable] = None,
    logFun: (SpanName, String, Map[String, String], Option[Throwable]) => G[Unit]
)(using span: SpanName) {

  // format: off
  /** Debug log level
   *
   * @param span
   *   the span to use for trace
   * @param message
   *   the message to log
   * @param ctx
   *   the context to log
   *
   * @example {{{
   *   LogLevel.Debug[F](span, errorMessage, ctx).log
   * }}}
   *
   */
  // format: on
  case Debug[F[_]: StructuredLogger: Trace, LOG_CONTEXT: LogContext](
      message: String,
      ctx: Option[LOG_CONTEXT] = None
  )(using span: SpanName)
      extends LogLevel(
        message,
        ctx,
        None,
        (s: SpanName, m: String, c: Map[String, String], _: Option[Throwable]) =>
          Trace[F].span(s.show)(StructuredLogger[F].debug(c)(m))
      )

  // format: off
  /** Info log level
   *
   * @param span
   *   the span to use for trace
   * @param message
   *   the message to log
   * @param ctx
   *   the context to log
   *
   * @example {{{
   *   LogLevel.Info[F](span, errorMessage, ctx).log
   * }}}
   *
   */
  // format: on
  case Info[F[_]: StructuredLogger: Trace, LOG_CONTEXT: LogContext](
      message: String,
      ctx: Option[LOG_CONTEXT] = None
  )(using span: SpanName)
      extends LogLevel(
        message,
        ctx,
        None,
        (s: SpanName, m: String, c: Map[String, String], _: Option[Throwable]) =>
          Trace[F].span(s.show)(StructuredLogger[F].info(c)(m))
      )

  // format: off
  /** Warn log level
   *
   * @param span
   *   the span to use for trace
   * @param message
   *   the message to log
   * @param ctx
   *   the context to log
   * @param error
   *   the error to add to the log
   *
   * @example {{{
   *   LogLevel.Warn[F](span, errorMessage, ctx, Some(e)).log
   * }}}
   *
   */
  // format: on
  case Warn[F[_]: StructuredLogger: Trace, LOG_CONTEXT: LogContext](
      message: String,
      ctx: Option[LOG_CONTEXT] = None,
      error: Option[Throwable] = None
  )(using span: SpanName)
      extends LogLevel(
        message,
        ctx,
        error,
        (s: SpanName, m: String, c: Map[String, String], e: Option[Throwable]) =>
          Trace[F].span(s.show)(StructuredLogger[F].warn(c, e.orNull)(m))
      )

  // format: off
  /** Error log level
    *
    * @param span
    *   the span to use for trace
    * @param message
    *   the message to log
    * @param ctx
    *   the context to log
    * @param error
    *   the error to add to the log
    *
    * @example {{{
    *   LogLevel.Error[F](span, errorMessage, ctx, Some(e)).log
    * }}}
    *
    */
  // format: on
  case Error[F[_]: StructuredLogger: Trace, LOG_CONTEXT: LogContext](
      message: String,
      ctx: Option[LOG_CONTEXT] = None,
      error: Option[Throwable] = None
  )(using span: SpanName)
      extends LogLevel(
        message,
        ctx,
        error,
        (s: SpanName, m: String, c: Map[String, String], e: Option[Throwable]) =>
          Trace[F].span(s.show)(StructuredLogger[F].error(c, e.orNull)(m))
      )

  def log: G[Unit] = logFun(span, message, ctx.map(_.logContext).getOrElse(Map.empty), error)
}
