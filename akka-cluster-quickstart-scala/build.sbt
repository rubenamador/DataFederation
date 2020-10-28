name := "akka-cluster-quickstart-scala"

version := "1.0"

organization := "com.example"

val projectScalaVersion = "2.12.1"
scalaVersion := "2.12.1"

lazy val AkkaVersion = "2.6.8"
lazy val SparkVersion = "2.4.4"
lazy val ZookeeperVersion = "3.6.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.typesafe.akka" %% "akka-cluster" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
  "org.apache.spark" %% "spark-core" % SparkVersion,
  "org.apache.spark" %% "spark-sql" % SparkVersion,
  "org.apache.zookeeper" % "zookeeper" % ZookeeperVersion
  
)

scalaVersion in ThisBuild := projectScalaVersion