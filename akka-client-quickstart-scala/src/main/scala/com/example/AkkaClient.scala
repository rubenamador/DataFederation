package com.example

import akka.actor._
import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.client._

import scala.io.StdIn.readLine

object AkkaClientTest extends App {
    val system = ActorSystem("PingPongSystem")
    
    // create the client
    val initialContacts = Set(
      ActorPath.fromString("akka://PingPongSystem@127.0.0.1:2551/system/receptionist"),
      ActorPath.fromString("akka://PingPongSystem@127.0.0.1:2552/system/receptionist"))
    val settings = ClusterClientSettings(system).withInitialContacts(initialContacts)
    val client = system.actorOf(ClusterClient.props(settings), "client")
	
	// start the action
	var message = ""
	while (message != "exit") {
		println("Enter your text message: ")
		message = readLine()
		client ! ClusterClient.Send("/user/ping", message, localAffinity = true) //sending first ping
	}
    
    // commented-out so you can see all the output
    system.terminate()
}