import Dependencies.*
import ReleaseTransformations.*

ThisBuild / scalaVersion := scala3

lazy val commonSettings = Seq(
  libraryDependencies ++= mainLibraries,
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
)

lazy val typelevelDemo = (project in file("modules/typelevel-demo"))
  .settings(
    name := "typelevel-demo",
    commonSettings,
    buildInfoPackage := "tech.kevinbreidenbach.typeleveldemo"
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging, BuildInfoPlugin)

lazy val typelevelDemoIt = (project in file("modules/typelevel-demo-it"))
  .dependsOn(typelevelDemo % "test->test;compile->compile")
  .settings(
    name := "typelevel-demo-it",
    commonSettings
  )

lazy val root = (project in file("."))
  .settings(
    name := "Typelevel Demo",
    releaseTagName := s"${version.value}",
    releaseNextCommitMessage := releaseNextCommitMessage.value + " [ci skip]",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepTask(typelevelDemo / Docker / publish),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
  .aggregate(typelevelDemo, typelevelDemoIt)

addCommandAlias("unitTest", "typelevelDemo/test")
