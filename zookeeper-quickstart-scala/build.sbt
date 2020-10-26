name := "zookeeper-quickstart-scala"

version := "1.0"

organization := "com.example"

val projectScalaVersion = "2.12.1"
scalaVersion := "2.12.1"

lazy val SparkVersion = "2.4.4"
lazy val ZookeeperVersion = "3.6.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % SparkVersion,
  "org.apache.spark" %% "spark-sql" % SparkVersion,
  "org.apache.zookeeper" % "zookeeper" % ZookeeperVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

scalaVersion in ThisBuild := projectScalaVersion