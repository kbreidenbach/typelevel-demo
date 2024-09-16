package tech.kevinbreidenbach.typeleveldemo.domain

import java.time.Instant
import java.util.UUID

import tech.kevinbreidenbach.typeleveldemo.util.LogContext

import cats.Show
import cats.Show.Shown
import cats.syntax.show.given
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.MatchesRegex

opaque type SpanName = String

object SpanName {

  def apply(value: String): SpanName              = value
  def unapply(spanType: SpanName): Option[String] = Some(spanType)

  extension (spanType: SpanName) {
    def value: String = spanType
  }

  given Show[SpanName] = _.value
}

val mainSpanName = SpanName("main_span")

enum Status {
  case Created
  case Updated
  case Deleted
}

object Status {
  given Show[Status] = _.toString
}

enum Action {
  case Add
  case Remove
}

object Action {
  given Show[Action] = _.toString
}

opaque type ID = UUID

object ID {

  def apply(value: UUID): ID = value

  def unapply(id: ID): Option[UUID] = Some(id)

  extension (id: ID) {
    def value: UUID = id
  }

  given Show[ID] = _.value.toString

  given LogContext[ID] with
    extension (id: ID) {
      def logContext: Map[String, String] = Map("id" -> id.show)
    }
}

opaque type PersonID = UUID

object PersonID {

  def apply(value: UUID): PersonID = value

  def unapply(personId: PersonID): Option[UUID] = Some(personId)

  extension (personId: PersonID) {
    def value: UUID = personId
  }

  given Show[PersonID] = _.value.toString

  given LogContext[PersonID] with
    extension (personId: PersonID) {
      def logContext: Map[String, String] = Map("person_id" -> personId.show)
    }
}

opaque type Firstname = String

object Firstname {

  def apply(value: String): Firstname = value

  def unapply(firstName: Firstname): Option[String] = Some(firstName)

  extension (firstName: Firstname) {
    def value: String = firstName
  }

  given Show[Firstname] = _.value

  given LogContext[Firstname] with
    extension (firstname: Firstname) {
      def logContext: Map[String, String] = Map("firstname" -> firstname.show)
    }
}

opaque type Lastname = String

object Lastname {

  def apply(value: String): Lastname = value

  def unapply(lastName: Lastname): Option[String] = Some(lastName)

  extension (lastName: Lastname) {
    def value: String = lastName
  }

  given Show[Lastname] = _.value

  given LogContext[Lastname] with
    extension (lastname: Lastname) {
      def logContext: Map[String, String] = Map("lastname" -> lastname.show)
    }
}

// a naive regex representation of an email address, but suffices for this exercise
type EmailRegex   = MatchesRegex["""[a-z0-9]+@[a-z0-9]+\\.[a-z0-9]{2,}"""]
type RefinedEmail = String Refined EmailRegex

opaque type Email = RefinedEmail

object Email {

  def apply(value: RefinedEmail): Email = value

  def unapply(email: Email): Option[RefinedEmail] = Some(email)

  extension (email: Email) {
    def value: RefinedEmail = email
  }

  given Show[Email] = _.value.value

  given LogContext[Email] with
    extension (email: Email) {
      def logContext: Map[String, String] = Map("email" -> email.show)
    }
}

opaque type CreatedOn = Instant

object CreatedOn {

  def apply(value: Instant): CreatedOn = value

  def unapply(createdOn: CreatedOn): Option[Instant] = Some(createdOn)

  extension (createdOn: CreatedOn) {
    def value: Instant = createdOn
  }

  given Show[CreatedOn] = _.value.toString
}

opaque type UpdatedOn = Instant

object UpdatedOn {

  def apply(value: Instant): UpdatedOn = value

  def unapply(updatedOn: UpdatedOn): Option[Instant] = Some(updatedOn)

  extension (updatedOn: UpdatedOn) {
    def value: Instant = updatedOn
  }

  given Show[UpdatedOn] = _.value.toString
}

opaque type DeletedOn = Instant

object DeletedOn {

  def apply(value: Instant): DeletedOn = value

  def unapply(deletedOn: DeletedOn): Option[Instant] = Some(deletedOn)

  extension (deletedOn: DeletedOn) {
    def value: Instant = deletedOn
  }

  given Show[DeletedOn] = _.value.toString
}

opaque type Points = Long

object Points {

  def apply(value: Long): Points = value

  def unapply(points: Points): Option[Long] = Some(points)

  extension (points: Points) {
    def value: Long = points
  }

  given Show[Points] = _.value.toString
}

case class Person(
    id: ID,
    email: Email,
    firstname: Firstname,
    lastname: Lastname,
    status: Status,
    createdOn: CreatedOn,
    updatedOn: Option[UpdatedOn],
    deletedOn: Option[DeletedOn]
)

object Person {
  given Show[Person] =
    person => show"""Person:
                    |\tid: ${person.id.show}
                    |\temail: ${person.email.show}
                    |\tfirstname: ${person.firstname.show}
                    |\tlastname: ${person.lastname.show}
                    |\tstatus: ${person.status.show}
                    |\tcreatedOn: ${person.createdOn.show}
                    |\tupdatedOn: ${person.updatedOn.show}
                    |\tdeletedOn: ${person.deletedOn.show}
                    |""".stripMargin

  given LogContext[Person] with
    extension (person: Person) {
      def logContext: Map[String, String] =
        Map(
          "id"        -> person.id.show,
          "email"     -> person.email.show,
          "firstname" -> person.firstname.show,
          "lastname"  -> person.lastname.show,
          "status"    -> person.status.show,
          "createdOn" -> person.createdOn.show,
          "updatedOn" -> person.updatedOn.show,
          "deletedOn" -> person.deletedOn.show
        )
    }
}

case class IncomingTransaction(
    personId: PersonID,
    points: Points,
    action: Action
)

object IncomingTransaction {
  given Show[IncomingTransaction] =
    transaction => show"""Transaction:
                         |\tpersonId: ${transaction.personId.show}
                         |\tpoints: ${transaction.points.show}
                         |\taction: ${transaction.action.show}
                         |""".stripMargin

  given LogContext[IncomingTransaction] with
    extension (transaction: IncomingTransaction) {
      def logContext: Map[String, String] =
        Map(
          "personId" -> transaction.personId.show,
          "points"   -> transaction.points.show,
          "action"   -> transaction.action.show
        )
    }
}

case class Transaction(
    id: ID,
    personId: PersonID,
    points: Points,
    action: Action,
    createdOn: CreatedOn
)

object Transaction {
  given Show[Transaction] =
    transaction => show"""Transaction:
                         |\tid: ${transaction.id.show}
                         |\tpersonId: ${transaction.personId.show}
                         |\tpoints: ${transaction.points.show}
                         |\taction: ${transaction.action.show}
                         |\tcreatedOn: ${transaction.createdOn.show}
                         |""".stripMargin

  given LogContext[Transaction] with
    extension (transaction: Transaction) {
      def logContext: Map[String, String] =
        Map(
          "id"        -> transaction.id.show,
          "personId"  -> transaction.personId.show,
          "points"    -> transaction.points.show,
          "action"    -> transaction.action.show,
          "createdOn" -> transaction.createdOn.show
        )
    }
}
