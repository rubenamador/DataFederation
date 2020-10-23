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
        case query if (query.toString.contains("CREATE TABLE") || query.toString.contains("SELECT") || query.toString.contains("DROP TABLE") || query.toString.contains("SHOW TABLES")) =>
            var response = "OK"
            try {
                val result = spark.sql(query.toString)
                result.show()

                // Save in string the headers of the data query
                var names = result.columns(0).toString
                for (i <- 1 to (result.columns.length - 1)) {
                    names = names + "," + result.columns(i).toString
                }
                var data = names + "\n"

                // Save in string each row of the data query
                val arr = result.collect() //Get the Array[Row] of the Dataframe
                for (i <- 0 to (arr.length - 1)) {
                    var row = arr(i)(0).toString
                    for (j <- 1 to (arr(i).length - 1)) {
                        row = row + "," + arr(i)(j).toString
                    }
                    data = data + row + "\n"
                }
                data = data + "END\n"
                response = data
            }
            catch
            {
                case e1: ClassNotFoundException => response = "ClassNotFoundException: Couldn't find that query. Wrong arguments "
                case e2: InterruptedException => response = "InterruptedException: Session interrupted. "
                case e3: org.apache.spark.sql.catalyst.parser.ParseException => response = "ParseException: Couldn't understand that query. Wrong syntax "
                case e4: org.apache.spark.sql.AnalysisException => response = "AnalysisException: Table or view not found. "
            }
            sender() ! response
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