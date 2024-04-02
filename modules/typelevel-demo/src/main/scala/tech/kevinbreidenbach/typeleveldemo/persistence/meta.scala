package tech.kevinbreidenbach.typeleveldemo.persistence

import java.time.Instant
import java.util.UUID
import scala.util.Try

import tech.kevinbreidenbach.typeleveldemo.domain.*

import doobie.Meta
import doobie.postgres.implicits.given
import doobie.postgres.implicits.pgEnumStringOpt
import doobie.refined.implicits.given
import eu.timepit.refined.api.Refined
import eu.timepit.refined.api.RefType
import eu.timepit.refined.string.MatchesRegex

given Meta[Status] = pgEnumStringOpt("status", status => Try(Status.valueOf(status)).toOption, _.toString)
given Meta[ID] = Meta[UUID].imap(ID.apply)(_.value)
given Meta[Firstname] = Meta[String].imap(Firstname.apply)(_.value)
given Meta[Lastname] = Meta[String].imap(Lastname.apply)(_.value)
given Meta[Email] = Meta[RefinedEmail].imap(Email.apply)(_.value)
given Meta[CreatedOn] = Meta[Instant].imap(CreatedOn.apply)(_.value)
given Meta[UpdatedOn] = Meta[Instant].imap(UpdatedOn.apply)(_.value)
given Meta[DeletedOn] = Meta[Instant].imap(DeletedOn.apply)(_.value)
