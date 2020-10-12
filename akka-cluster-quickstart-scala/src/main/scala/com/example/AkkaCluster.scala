package com.example

import akka.actor._
import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.client._

import scala.io.StdIn.readLine

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.log4j.{Level, Logger}

class SparkActor extends Actor {
    // start the Spark session
    lazy val spark: SparkSession = SparkSession
        .builder()
        .master("local[*]")
        .appName("Spark SQL basic example")
        .getOrCreate()
        //.config("spark.some.config.option", "some-value")
    
    import spark.implicits._
    import org.apache.spark.sql.types._
    
    def receive = {
        case query if query == "EXIT" =>
            spark.stop()
            println("Finish the Spark Session ...")
			sender() ! "EXIT"
        case query if query != "EXIT" =>
            try {
                val result = spark.sql(query.toString)
                result.show()
            }
            catch
            {
                case e1: ClassNotFoundException => println("Couldn't find that query. Wrong arguments ")
                case e2: InterruptedException => println("Session interrupted. ")
                case e3: org.apache.spark.sql.catalyst.parser.ParseException => println("Couldn't understand that query. Wrong syntax ")
                case e4: org.apache.spark.sql.AnalysisException => println("Table or view not found. ")
            }
			sender() ! "OK"
    }
}

object AkkaClusterTest extends App {
    val system = ActorSystem("SparkSessionSystem")
    val spark_actor = system.actorOf(Props[SparkActor], name = "spark")
    
    // create the cluster
    val cluster = Cluster(system)
    cluster.join(cluster.selfAddress)
    ClusterClientReceptionist(system).registerService(spark_actor)
    
    // stop the cluster
    var message = ""
    while (message != "exit") {
        message = readLine()
    }
    
    // commented-out so you can see all the output
    system.terminate()
}