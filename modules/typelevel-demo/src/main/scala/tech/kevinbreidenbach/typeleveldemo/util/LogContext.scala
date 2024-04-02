package tech.kevinbreidenbach.typeleveldemo.util

import scala.annotation.unused

trait LogContext[T] {
  extension (@unused t: T) def logContext: Map[String, String]
}

object LogContext {
  given LogContext[Unit] with
    extension (@unused u: Unit) {
      def logContext: Map[String, String] = Map.empty
    }
}
