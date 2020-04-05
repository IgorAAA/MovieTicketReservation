name := "VeonAssignment"

version := "0.2"

scalaVersion := "2.12.10"

// Versions
val circeVersion = "0.13.0"
val http4sVersion = "0.21.3"
val pureConfigVersion = "0.12.2"
val scalaTestVersion = "3.1.0"
val catsScalaTestVersion = "3.0.5"
val logs4CatsVersion = "1.0.1"
val logbackVersion = "1.2.3"

// Circe
val circeLibs = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-fs2"
).map(_ % circeVersion)

// Http4s
val http4sLibs = Seq(
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-circe"
).map(_ % http4sVersion)

// Pure config
val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion

// Logging
val logs4Cats = Seq(
  "io.chrisdavenport" %% "log4cats-core" % logs4CatsVersion,
  "io.chrisdavenport" %% "log4cats-slf4j" % logs4CatsVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

// Test
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
val catsScalaTest = "com.ironcorelabs" %% "cats-scalatest" % catsScalaTestVersion % "test"

val testLibs = Seq(
  scalaTest,
  catsScalaTest
)

libraryDependencies ++= circeLibs ++ http4sLibs ++ logs4Cats ++ testLibs :+ pureConfig
