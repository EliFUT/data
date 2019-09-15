import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val akkahttp = "com.typesafe.akka" %% "akka-http"  % "10.1.9"
  lazy val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.23"
}
