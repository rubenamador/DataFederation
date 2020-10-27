name := "zookeeper-quickstart-scala"

version := "1.0"

organization := "com.example"

val projectScalaVersion = "2.12.1"
scalaVersion := "2.12.1"

lazy val ZookeeperVersion = "3.6.1"

libraryDependencies ++= Seq(
  "org.apache.zookeeper" % "zookeeper" % ZookeeperVersion
)

scalaVersion in ThisBuild := projectScalaVersion