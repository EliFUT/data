import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  lazy val akkahttp = "com.typesafe.akka" %% "akka-http"  % "10.1.9"
  lazy val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.25"
  lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.5.25"
  lazy val apacheCommonsIO = "commons-io" % "commons-io" % "2.6"
}
