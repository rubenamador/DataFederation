package com.example

import akka.actor._
import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.client._

case object PingMessage
case object PongMessage
case object StartMessage
case object StopMessage

object ClientSystem extends App {
    val system = ActorSystem("PingPongSystem")
    
    // create the client
    val initialContacts = Set(
      ActorPath.fromString("akka://PingPongSystem@127.0.0.1:2551/system/receptionist"),
      ActorPath.fromString("akka://PingPongSystem@127.0.0.1:2552/system/receptionist"))
    val settings = ClusterClientSettings(system).withInitialContacts(initialContacts)
    val client = system.actorOf(ClusterClient.props(settings), "client")
    
    // start the action
    //ping ! StartMessage
    client ! ClusterClient.Send("/user/ping", StartMessage, localAffinity = true) //sending first ping
    
    // commented-out so you can see all the output
    system.terminate()
}