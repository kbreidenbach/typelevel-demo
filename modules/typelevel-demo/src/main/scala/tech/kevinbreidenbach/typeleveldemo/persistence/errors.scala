package tech.kevinbreidenbach.typeleveldemo.persistence

case class DatabasePersistenceError(message: String, cause: Option[Throwable] = None)
    extends RuntimeException(message, cause.orNull)
