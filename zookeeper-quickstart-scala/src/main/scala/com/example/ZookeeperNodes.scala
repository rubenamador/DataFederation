package com.example

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.log4j.{Level, Logger}

import scala.io.StdIn.readLine

import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.AsyncCallback.StatCallback
import org.apache.zookeeper.KeeperException.Code
import org.apache.zookeeper.data.Stat
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.ZooDefs

import java.util.concurrent.CountDownLatch;

class ZooKeeperConnection {
    var zoo: ZooKeeper = _
    val connectedSignal = new CountDownLatch(1)

    // Method to connect zookeeper ensemble
    def connect(host: String) : ZooKeeper = {    
        var zoo = new ZooKeeper(host, 5000, new Watcher() {        
            def process(we: WatchedEvent) = {
                if (we.getState() == KeeperState.SyncConnected) {
                    connectedSignal.countDown()
                }
            }        
        })
        //connectedSignal.await()
        println("Connecting Zookeeper ...")
        return zoo
    }

    // Method to disconnect from zookeeper server
    def close() = {
        zoo.close()
    }
}

object ZookeeperServerTest extends App {
    println("Beginning...")
    val conn = new ZooKeeperConnection()
    val zk = conn.connect("localhost")
    println("Creating Zookeeper Connection...")
    
    val path = "/MyFirstZnode"
    val data = "My first zookeeper app".getBytes()
    println("Creating ZKPath...")
    
    val stat : Stat = zk.exists(path, true)
    println("Checking ZNode exists...")
    
    if (stat != null) {
        println("Node exists and the node version is " + stat.getVersion())
    }
    else {
        println("Node does not exists")
        zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
        println("Creating ZNode in one path...")
    }

    conn.close()
}