package com.example

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.log4j.{Level, Logger}

import scala.io.StdIn.readLine

object SparkSessionTest extends App {
	
	//Logger.getLogger("INFO").setLevel(Level.OFF)
	//Logger.getLogger("WARN").setLevel(Level.OFF)
	//Logger.getLogger("ERROR").setLevel(Level.OFF)
	
	// Start Spark Session
	lazy val spark: SparkSession = SparkSession
		.builder()
		.master("local[*]")
		.appName("Spark SQL basic example")
		.getOrCreate()
		//.config("spark.some.config.option", "some-value")
	
	import spark.implicits._
	import org.apache.spark.sql.types._
	
	var query = ""

	while (query != "EXIT") {
		// Receive Queries
		println("Enter your SQL Query: ")
		query = readLine()

		if (query.contains("CREATE TABLE") || query.contains("SELECT") || query.contains("DROP TABLE") || query.contains("SHOW TABLES")) {
			try {
				val result = spark.sql(query)
				result.show()
			}
			catch
			{
				case e1: ClassNotFoundException => println("Couldn't find that query. Wrong arguments ")
				case e2: InterruptedException => println("Session interrupted. ")
				case e3: org.apache.spark.sql.catalyst.parser.ParseException => println("Couldn't understand that query. Wrong syntax ")
				case e4: org.apache.spark.sql.AnalysisException => println("Table or view not found. ")
			}
		}
		else if (query.contains("EXIT")) {
			println("Finish the Spark Session ...")
		}
		else {
			println("This query is not accepted. ")
		}
	}
	
	spark.stop()
	
}