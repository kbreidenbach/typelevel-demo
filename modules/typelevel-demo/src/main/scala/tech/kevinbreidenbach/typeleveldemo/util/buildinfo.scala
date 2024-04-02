package tech.kevinbreidenbach.typeleveldemo.util

import tech.kevinbreidenbach.typeleveldemo.BuildInfo

import cats.Show
import cats.syntax.show.given

opaque type AppName = String

object AppName {
  def apply(value: String): AppName = value

  extension (appName: AppName) {
    def value: String = appName
  }

  given Show[AppName] = _.value
}

opaque type AppVersion = String

object AppVersion {
  def apply(value: String): AppVersion = value

  extension (appVersion: AppVersion) {
    def value: String = appVersion
  }

  given Show[AppVersion] = _.value
}

opaque type ScalaVersion = String

object ScalaVersion {
  def apply(value: String): ScalaVersion = value

  extension (scalaVersion: ScalaVersion) {
    def value: String = scalaVersion
  }

  given Show[ScalaVersion] = _.value
}

opaque type SbtVersion = String

object SbtVersion {
  def apply(value: String): SbtVersion = value

  extension (sbtVersion: SbtVersion) {
    def value: String = sbtVersion
  }

  given Show[SbtVersion] = _.value
}

val appName      = AppName(BuildInfo.name)
val appVersion   = AppVersion(BuildInfo.version)
val scalaVersion = ScalaVersion(BuildInfo.scalaVersion)
val sbtVersion   = SbtVersion(BuildInfo.sbtVersion)

val buildInfoContext: Map[String, String] = Map(
  "appName"      -> appName.show,
  "appVersion"   -> appVersion.show,
  "scalaVersion" -> scalaVersion.show,
  "sbtVersion"   -> sbtVersion.show
)
