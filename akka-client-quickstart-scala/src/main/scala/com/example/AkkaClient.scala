package com.example

import akka.actor._
import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.client._

import scala.io.StdIn.readLine

object AkkaClientTest extends App {
    val system = ActorSystem("SparkSessionSystem")
    
    // create the client
    val initialContacts = Set(
      ActorPath.fromString("akka://SparkSessionSystem@127.0.0.1:2551/system/receptionist"),
      ActorPath.fromString("akka://SparkSessionSystem@127.0.0.1:2552/system/receptionist"))
    val settings = ClusterClientSettings(system).withInitialContacts(initialContacts)
    val client = system.actorOf(ClusterClient.props(settings), "client")
    
    // start the action
    var query = ""
    while (query != "EXIT") {
        println("Enter your SQL Query: ")
        query = readLine()
        
        if (query.contains("CREATE TABLE") || query.contains("SELECT") || query.contains("DROP TABLE") || query.contains("SHOW TABLES")) {
            client ! ClusterClient.Send("/user/spark", query, localAffinity = true) //sending SQL query
        }
        else if (query.contains("EXIT")) {
            client ! ClusterClient.Send("/user/spark", query, localAffinity = true) //sending SQL query for exit
        }
        else {
            println("This query is not accepted. ")
        }
    }
    
    // commented-out so you can see all the output
    system.terminate()
}