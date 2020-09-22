name := "spark-quickstart-scala"

version := "1.0"

organization := "com.example"

val projectScalaVersion = "2.12.1"
scalaVersion := "2.12.1"

lazy val SparkVersion = "2.4.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % SparkVersion,
  "org.apache.spark" %% "spark-sql" % SparkVersion
)

scalaVersion in ThisBuild := projectScalaVersion