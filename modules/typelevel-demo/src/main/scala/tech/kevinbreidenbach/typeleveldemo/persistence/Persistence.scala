package tech.kevinbreidenbach.typeleveldemo.persistence

import java.sql.SQLException

import tech.kevinbreidenbach.typeleveldemo.domain.Email
import tech.kevinbreidenbach.typeleveldemo.domain.Email.given
import tech.kevinbreidenbach.typeleveldemo.domain.Firstname
import tech.kevinbreidenbach.typeleveldemo.domain.ID
import tech.kevinbreidenbach.typeleveldemo.domain.IncomingTransaction
import tech.kevinbreidenbach.typeleveldemo.domain.Lastname
import tech.kevinbreidenbach.typeleveldemo.domain.Person
import tech.kevinbreidenbach.typeleveldemo.domain.PersonID
import tech.kevinbreidenbach.typeleveldemo.domain.Points
import tech.kevinbreidenbach.typeleveldemo.domain.SpanName
import tech.kevinbreidenbach.typeleveldemo.domain.Transaction
import tech.kevinbreidenbach.typeleveldemo.util.LogContext
import tech.kevinbreidenbach.typeleveldemo.util.LogLevel
import tech.kevinbreidenbach.typeleveldemo.util.RunWithRetry

import cats.effect.Async
import cats.syntax.all.given
import doobie.implicits.given
import doobie.util.transactor.Transactor
import natchez.Trace
import org.typelevel.log4cats.StructuredLogger

trait Persistence[F[_]] {

  def upsertPerson(person: Person): F[Either[DatabasePersistenceError, Person]]
  def findPersonByFirstname(firstname: Firstname): F[Either[DatabasePersistenceError, List[Person]]]
  def findPersonByLastname(lastname: Lastname): F[Either[DatabasePersistenceError, List[Person]]]
  def findPersonByEmail(email: Email): F[Either[DatabasePersistenceError, Option[Person]]]
  def findPersonById(id: ID): F[Either[DatabasePersistenceError, Option[Person]]]
  def deletePersonByEmail(email: Email): F[Either[DatabasePersistenceError, Option[Person]]]
  def deletePersonByID(id: ID): F[Either[DatabasePersistenceError, Option[Person]]]
  def insertPointsTransaction(transaction: IncomingTransaction): F[Either[DatabasePersistenceError, Transaction]]
  def findPointsTotalForPerson(personId: PersonID): F[Either[DatabasePersistenceError, Points]]
}

object Persistence {
  def make[F[_]: Async: StructuredLogger: Trace: RunWithRetry](transactor: Transactor[F]): Persistence[F] =
    new PostgresPersistence[F](transactor)
}

class PostgresPersistence[F[_]: Async: StructuredLogger: Trace: RunWithRetry] private[persistence] (
    transactor: Transactor[F]
) extends Persistence[F] {

  private given span: SpanName = SpanName("postgres-persistence")

  extension [T: LogContext, R](either: Either[SQLException, R]) {
    private def logAndAdapt(message: String, ctx: T): F[Either[DatabasePersistenceError, R]] =
      either match {
        case Left(e)     =>
          LogLevel.Error(message, ctx.some, e.some).log >> DatabasePersistenceError("e", e.some).asLeft[R].pure
        case Right(r: R) => r.asRight[DatabasePersistenceError].pure
      }
  }

  override def upsertPerson(person: Person): F[Either[DatabasePersistenceError, Person]] =
    Trace[F].span(span.show) {
      RunWithRetry[F].retry("upsertPerson") {
        upsertPersonSql(person)
          .query[Person]
          .unique
          .transact(transactor)
          .attemptSql
          .flatMap(_.logAndAdapt(show"error upserting ${person}", person))
      }
    }

  override def findPersonByFirstname(firstname: Firstname): F[Either[DatabasePersistenceError, List[Person]]] =
    Trace[F].span(span.show) {
      findPersonByFirstnameSql(firstname)
        .query[Person]
        .to[List]
        .transact(transactor)
        .attemptSql
        .flatMap(_.logAndAdapt(show"error finding person with firstname ${firstname}", firstname))
    }

  override def findPersonByLastname(lastname: Lastname): F[Either[DatabasePersistenceError, List[Person]]] =
    Trace[F].span(span.show) {
      findPersonByLastnameSql(lastname)
        .query[Person]
        .to[List]
        .transact(transactor)
        .attemptSql
        .flatMap(_.logAndAdapt(show"error finding person with lastname ${lastname}", lastname))
    }

  override def findPersonByEmail(email: Email): F[Either[DatabasePersistenceError, Option[Person]]] =
    Trace[F].span(span.show) {
      findPersonByEmailSql(email)
        .query[Person]
        .option
        .transact(transactor)
        .attemptSql
        .flatMap(_.logAndAdapt(show"error finding person with email ${email}", email))
    }

  override def findPersonById(id: ID): F[Either[DatabasePersistenceError, Option[Person]]] =
    Trace[F].span(span.show) {
      findPersonByIdSql(id)
        .query[Person]
        .option
        .transact(transactor)
        .attemptSql
        .flatMap(_.logAndAdapt(show"error finding person with id ${id}", id))
    }

  override def deletePersonByEmail(email: Email): F[Either[DatabasePersistenceError, Option[Person]]] =
    Trace[F].span(span.show) {
      RunWithRetry[F].retry("deletePersonByEmail") {
        deletePersonByEmailSql(email)
          .query[Person]
          .option
          .transact(transactor)
          .attemptSql
          .flatMap(_.logAndAdapt(show"error deleting person with email ${email}", email))
      }
    }

  override def deletePersonByID(id: ID): F[Either[DatabasePersistenceError, Option[Person]]] =
    Trace[F].span(span.show) {
      RunWithRetry[F].retry("deletePersonByID") {
        deletePersonByIdSql(id)
          .query[Person]
          .option
          .transact(transactor)
          .attemptSql
          .flatMap(_.logAndAdapt(show"error deleting person with id ${id}", id))
      }
    }

  override def insertPointsTransaction(
      transaction: IncomingTransaction
  ): F[Either[DatabasePersistenceError, Transaction]] =
    Trace[F].span(span.show) {
      RunWithRetry[F].retry("upsertPerson") {
        insertPointsTransactionSql(transaction)
          .query[Transaction]
          .unique
          .transact(transactor)
          .attemptSql
          .flatMap(_.logAndAdapt(show"error inserting ${transaction}", transaction))
      }
    }

  override def findPointsTotalForPerson(personId: PersonID): F[Either[DatabasePersistenceError, Points]] =
    Trace[F].span(span.show) {
      findPointsTotalSql(personId)
        .query[Points]
        .option
        .transact(transactor)
        .attemptSql
        .map(_.map(_.getOrElse(Points(0L))))
        .flatMap(_.logAndAdapt(show"error finding points total for person with id ${personId}", personId))
    }
}
