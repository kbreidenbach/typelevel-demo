package tech.kevinbreidenbach.typeleveldemo.http

import java.time.Instant
import java.util.UUID

import tech.kevinbreidenbach.typeleveldemo.domain.*
import tech.kevinbreidenbach.typeleveldemo.health.HealthStatus
import tech.kevinbreidenbach.typeleveldemo.health.Status as HStatus

import cats.syntax.all.given
import eu.timepit.refined.refineV
import eu.timepit.refined.string.MatchesRegex
import sttp.tapir.Codec
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.DecodeResult
import sttp.tapir.Schema

extension (email: String) {
  def toEmail: Either[String, Email] =
    refineV[EmailRegex](email) match {
      case Left(error)         => error.asLeft
      case Right(refinedEmail) => Email(refinedEmail).asRight
    }
}

given Codec[String, Status, TextPlain] = Codec.string.map(Status.valueOf)(_.toString)
given Codec[String, HStatus, TextPlain] = Codec.string.map(HStatus.valueOf)(_.toString)
given Codec[String, ID, TextPlain] = Codec.uuid.map(ID.apply)(_.value)
given Codec[String, Firstname, TextPlain] = Codec.string.map(Firstname.apply)(_.value)
given Codec[String, Lastname, TextPlain] = Codec.string.map(Lastname.apply)(_.value)

given Codec[String, Email, TextPlain] =
  Codec.string.mapDecode { s =>
    s.toEmail match {
      case Left(error)  => DecodeResult.Error(s, new IllegalArgumentException(error))
      case Right(email) => DecodeResult.Value(email)
    }
  }(email => email.value.value)

given Codec[String, CreatedOn, TextPlain] = Codec.instant.map(CreatedOn.apply)(_.value)
given Codec[String, UpdatedOn, TextPlain] = Codec.instant.map(UpdatedOn.apply)(_.value)
given Codec[String, DeletedOn, TextPlain] = Codec.instant.map(DeletedOn.apply)(_.value)

given Schema[Status] =
  Schema.derivedEnumeration[Status](encode = Some(v => v)).description("The person's status in the system")

given Schema[HStatus] =
  Schema.derivedEnumeration[HStatus](encode = Some(v => v)).description("The status of a resource or the system")

given Schema[ID] = Schema.schemaForUUID.map(u => ID(u).some)(_.value).description("The person's ID in the system")

given Schema[Firstname] =
  Schema.schemaForString.map(s => Firstname(s).some)(_.value).description("The person's first name")

given Schema[Lastname] =
  Schema.schemaForString.map(s => Lastname(s).some)(_.value).description("The person's last name")

given Schema[Email] =
  Schema.schemaForString
    .map {
      _.toEmail match {
        case Left(_)      => None
        case Right(email) => email.some
      }
    }(_.value.value)
    .description("The person's email address")

given Schema[CreatedOn] =
  Schema.schemaForInstant
    .map(i => CreatedOn(i).some)(_.value)
    .description("The timestamp when the person was created in the system")

given Schema[UpdatedOn] =
  Schema.schemaForInstant
    .map(i => UpdatedOn(i).some)(_.value)
    .description("The timestamp when the person was last updated in the system")

given Schema[DeletedOn] =
  Schema.schemaForInstant
    .map(i => DeletedOn(i).some)(_.value)
    .description("The timestamp when the person was deleted in the system")

given Schema[Person] = Schema.derived
given Schema[HealthStatus] = Schema.derived
