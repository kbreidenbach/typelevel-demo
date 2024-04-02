import sbt.*

object Dependencies {

  val scala3 = "3.3.3"

  val catsVersion              = "2.10.0"
  val catsEffectVersion        = "3.5.2"
  val catsRetryVersion         = "3.1.0"
  val fs2Version               = "3.9.2"
  val fs2KafkaVersion          = "3.2.0"
  val circeVersion             = "0.14.6"
  val declineVersion           = "2.4.1"
  val log4catsVersion          = "2.6.0"
  val natchezExtrasVersion     = "8.1.0"
  val logbackClassicVersion    = "1.4.11"
  val logstashEncoderVersion   = "7.4"
  val doobieVersion            = "1.0.0-RC4"
  val http4sVersion            = "0.23.16"
  val javaUuidGeneratorVersion = "5.0.0"
  val apiSpecVersion           = "0.11.3"
  val tapirVersion             = "1.11.1"
  val flywayVersion            = "10.0.0"
  val refinedVersion           = "0.11.1"

  lazy val mainLibraries: Seq[ModuleID] = Seq(
    "org.typelevel"                 %% "cats-core"                     % catsVersion,
    "org.typelevel"                 %% "cats-effect"                   % catsEffectVersion,
    "com.github.cb372"              %% "cats-retry"                    % catsRetryVersion,
    "co.fs2"                        %% "fs2-core"                      % fs2Version,
    "com.github.fd4s"               %% "fs2-kafka"                     % fs2KafkaVersion,
    "org.tpolecat"                  %% "doobie-hikari"                 % doobieVersion,
    "org.tpolecat"                  %% "doobie-postgres"               % doobieVersion,
    "org.tpolecat"                  %% "doobie-refined"                % doobieVersion,
    "org.flywaydb"                   % "flyway-database-postgresql"    % flywayVersion,
    "org.http4s"                    %% "http4s-core"                   % http4sVersion,
    "org.http4s"                    %% "http4s-ember-server"           % http4sVersion,
    "org.http4s"                    %% "http4s-ember-client"           % http4sVersion,
    "org.http4s"                    %% "http4s-circe"                  % http4sVersion,
    "io.circe"                      %% "circe-core"                    % circeVersion,
    "io.circe"                      %% "circe-generic"                 % circeVersion,
    "io.circe"                      %% "circe-parser"                  % circeVersion,
    "com.monovore"                  %% "decline-effect"                % declineVersion,
    "org.typelevel"                 %% "log4cats-slf4j"                % log4catsVersion,
    "com.ovoenergy"                 %% "natchez-extras-log4cats"       % natchezExtrasVersion,
    "com.ovoenergy"                 %% "natchez-extras-datadog-stable" % natchezExtrasVersion,
    "ch.qos.logback"                 % "logback-classic"               % logbackClassicVersion,
    "net.logstash.logback"           % "logstash-logback-encoder"      % logstashEncoderVersion,
    "com.fasterxml.uuid"             % "java-uuid-generator"           % javaUuidGeneratorVersion,
    "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml"            % apiSpecVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-core"                    % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"           % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"              % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"            % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui"              % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-cats"                    % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-apispec-docs"            % tapirVersion,
    "eu.timepit"                    %% "refined"                       % refinedVersion
  )
}
