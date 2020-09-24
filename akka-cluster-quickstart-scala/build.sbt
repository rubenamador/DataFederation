name := "akka-cluster-quickstart-scala"

version := "1.0"

organization := "com.example"

val projectScalaVersion = "2.13.1"
scalaVersion := "2.13.1"

lazy val AkkaVersion = "2.6.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.typesafe.akka" %% "akka-cluster" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion
)

scalaVersion in ThisBuild := projectScalaVersion