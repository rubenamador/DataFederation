package com.example

import akka.actor._
import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.client._

import scala.io.StdIn.readLine

class Ping extends Actor {
    def receive = {
        case "pong" =>
            println("ping")
    }
}

class Pong extends Actor {
    def receive = {
        case "ping" =>
            println("pong")
    }
}

object AkkaClusterTest extends App {
    val system = ActorSystem("PingPongSystem")
    val pong = system.actorOf(Props[Pong], name = "pong")
    val ping = system.actorOf(Props[Ping], name = "ping")
    
    // create the cluster
    val cluster = Cluster(system)
    cluster.join(cluster.selfAddress)
    ClusterClientReceptionist(system).registerService(pong)
    ClusterClientReceptionist(system).registerService(ping)
    
    // stop the cluster
    var message = ""
    while (message != "exit") {
        message = readLine()
    }
    
    // commented-out so you can see all the output
    system.terminate()
}