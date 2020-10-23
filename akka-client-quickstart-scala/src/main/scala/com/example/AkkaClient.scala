package com.example

import akka.actor._
import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.client._

import scala.io.StdIn.readLine

class ClientActor(client: ActorRef) extends Actor {
    def receive = {
        case message if (message.toString.contains("CREATE TABLE") || message.toString.contains("SELECT") || message.toString.contains("DROP TABLE") || message.toString.contains("SHOW TABLES")) =>
            client ! ClusterClient.Send("/user/spark", message, localAffinity = true) //sending SQL query
        case message if (message.toString.contains("Exception")) =>
            println(message)
        case message if message == "EXIT" =>
            client ! ClusterClient.Send("/user/spark", message, localAffinity = true) //sending SQL query
            context.stop(self)
        case message if (message.toString.contains("END")) =>
            val iterator = message.toString.split("\n").toIterator
            while(iterator.hasNext) {
                println(iterator.toIterator.next())
            }
        case _ =>
            println("This query is not accepted. ")
    }
}

object AkkaClientTest extends App {
    val system = ActorSystem("SparkSessionSystem")
    
    // create the client
    val initialContacts = Set(
      ActorPath.fromString("akka://SparkSessionSystem@127.0.0.1:2551/system/receptionist"),
      ActorPath.fromString("akka://SparkSessionSystem@127.0.0.1:2552/system/receptionist"))
    val settings = ClusterClientSettings(system).withInitialContacts(initialContacts)
    val client = system.actorOf(ClusterClient.props(settings), "client")
    val cli = system.actorOf(Props(new ClientActor(client)), name = "cli")
    
    // start the action
    var message = ""
    while (message != "EXIT") {
        println("Enter your SQL Query: ")
        message = readLine()
        cli ! message
        Thread.sleep(4000) // wait for 4 seconds
    }
    
    // commented-out so you can see all the output
    system.terminate()
}