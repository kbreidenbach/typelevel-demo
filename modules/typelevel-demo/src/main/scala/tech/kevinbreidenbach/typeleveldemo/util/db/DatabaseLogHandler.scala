package tech.kevinbreidenbach.typeleveldemo.util.db

import scala.concurrent.duration.*

import tech.kevinbreidenbach.typeleveldemo.util.db.DatabaseLogHandler.*

import cats.Show
import cats.syntax.show.given
import doobie.LogHandler
import doobie.util.log.ExecFailure
import doobie.util.log.LogEvent
import doobie.util.log.ProcessingFailure
import doobie.util.log.Success
import org.typelevel.log4cats.StructuredLogger

object DatabaseLogHandler {
  def make[F[_]: StructuredLogger]: LogHandler[F] = new DatabaseLogHandler[F]()

  opaque type Sql = String

  private object Sql {
    def apply(value: String): Sql = value

    extension (sql: Sql) {
      private def value: String = sql
    }

    given Show[Sql] = Show.show(_.value)
  }

  opaque type Args = List[?]

  private object Args {
    def apply(value: List[?]): Args = value

    extension (args: Args) {
      private def value: List[?] = args
    }

    given Show[Args] = Show.show(_.value.mkString(", "))
  }

  opaque type Label = String

  private object Label {
    def apply(value: String): Label = value

    extension (label: Label) {
      private def value: String = label
    }

    given Show[Label] = Show.show(_.value)
  }

  opaque type ExecutionTime = FiniteDuration

  private object ExecutionTime {
    def apply(value: FiniteDuration): ExecutionTime = value

    extension (executionTime: ExecutionTime) {
      def value: FiniteDuration = executionTime
    }

    given Show[ExecutionTime] = Show.show(_.toMillis.toString)
  }

  opaque type ProcessingTime = FiniteDuration

  private object ProcessingTime {
    def apply(value: FiniteDuration): ProcessingTime = value

    extension (processingTime: ProcessingTime) {
      def value: FiniteDuration = processingTime
    }

    given Show[ProcessingTime] = Show.show(_.toMillis.toString)
  }
}

class DatabaseLogHandler[F[_]: StructuredLogger] private extends LogHandler[F] {
  override def run(logEvent: LogEvent): F[Unit] = {
    def log(
        title: String,
        sql: String,
        args: List[Any],
        label: String,
        execution: FiniteDuration,
        processing: FiniteDuration,
        logger: (Map[String, String], String) => F[Unit]
    ): F[Unit] = {
      val s     = Sql(sql)
      val a     = Args(args)
      val l     = Label(label)
      val e     = ExecutionTime(execution)
      val p     = ProcessingTime(processing)
      val total = (e.value + p.value).toMillis

      val logContext: Map[String, String] = Map(
        "db.sql"             -> s.show,
        "db.args"            -> a.show,
        "db.label"           -> l.show,
        "db.execution-time"  -> e.show,
        "db.processing-time" -> p.show
      )

      logger(
        logContext,
        s"""$title:
           |
           |  ${s.show}
           |
           | arguments = [${a.show}]
           | label     = ${l.show}
           |   elapsed = ${e.show} ms exec + ${p.show} ms processing, (${total.toString} ms total)
           |""".stripMargin
      )
    }

    logEvent match {
      case Success(sql, args, label, executionTime, processingTime) =>
        log(
          "Successful Execution",
          sql,
          args,
          label,
          executionTime,
          processingTime,
          StructuredLogger[F].info(_: Map[String, String])(_: String)
        )

      case ProcessingFailure(sql, args, label, executionTime, processingTime, error) =>
        log(
          "Processing Failure",
          sql,
          args,
          label,
          executionTime,
          processingTime,
          StructuredLogger[F].error(_: Map[String, String], error)(_: String)
        )

      case ExecFailure(sql, args, label, executionTime, error) =>
        log(
          "Processing Failure",
          sql,
          args,
          label,
          executionTime,
          0.seconds,
          StructuredLogger[F].error(_: Map[String, String], error)(_: String)
        )
    }
  }
}
