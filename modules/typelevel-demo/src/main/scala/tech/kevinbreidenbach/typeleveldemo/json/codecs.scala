package tech.kevinbreidenbach.typeleveldemo.json

import java.time.Instant
import java.util.UUID
import scala.util.Try

import tech.kevinbreidenbach.typeleveldemo.domain.CreatedOn
import tech.kevinbreidenbach.typeleveldemo.domain.DeletedOn
import tech.kevinbreidenbach.typeleveldemo.domain.Email
import tech.kevinbreidenbach.typeleveldemo.domain.Firstname
import tech.kevinbreidenbach.typeleveldemo.domain.ID
import tech.kevinbreidenbach.typeleveldemo.domain.Lastname
import tech.kevinbreidenbach.typeleveldemo.domain.RefinedEmail
import tech.kevinbreidenbach.typeleveldemo.domain.Status
import tech.kevinbreidenbach.typeleveldemo.domain.UpdatedOn
import tech.kevinbreidenbach.typeleveldemo.health.HealthStatus
import tech.kevinbreidenbach.typeleveldemo.health.Status as HStatus

import cats.syntax.all.given
import eu.timepit.refined.api.RefType
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveCodec

private[json] inline def codec[T: Encoder: Decoder]: Codec[T] = Codec.from(Decoder[T], Encoder[T])

given Codec[Status] = codec[String].iemap(s => Try(Status.valueOf(s)).toEither.leftMap(_.getMessage))(_.toString)
given Codec[HStatus] = codec[String].iemap(s => Try(HStatus.valueOf(s)).toEither.leftMap(_.getMessage))(_.toString)
given Codec[ID] = codec[UUID].iemap(s => ID(s).asRight[String])(_.value)
given Codec[Firstname] = codec[String].iemap(s => Firstname(s).asRight[String])(_.value)
given Codec[Lastname] = codec[String].iemap(s => Lastname(s).asRight[String])(_.value)

given Codec[Email] =
  codec[String].iemap(e => RefType.applyRef[RefinedEmail](e).map((e: RefinedEmail) => Email(e)))(_.value.value)

given Codec[CreatedOn] = codec[Instant].iemap(i => CreatedOn(i).asRight[String])(_.value)
given Codec[UpdatedOn] = codec[Instant].iemap(i => UpdatedOn(i).asRight[String])(_.value)
given Codec[DeletedOn] = codec[Instant].iemap(i => DeletedOn(i).asRight[String])(_.value)
given Codec[HealthStatus] = deriveCodec[HealthStatus]
