package com.example

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

import java.util.concurrent.CountDownLatch
import scala.io.StdIn.readLine

class ZooKeeperConnection {
    var zoo: ZooKeeper = _
    val connectedSignal = new CountDownLatch(1)

    // Method to connect zookeeper ensemble
    def connect(host: String) : ZooKeeper = {    
        var zoo = new ZooKeeper(host, 2181, new Watcher() { //in my case ZK uses port 2181 --- you can put another port
            def process(we: WatchedEvent) = {
                if (we.getState() == KeeperState.SyncConnected) {
                    connectedSignal.countDown()
                }
            }        
        })
        connectedSignal.await()
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
    
    // Creating Zookeeper connection
    val conn = new ZooKeeperConnection()
    val zk = conn.connect("192.168.99.100") //this host is my docker-machine ip --- you can put another host as "localhost"
    println("Creating Zookeeper Connection...")
    
    // Creating path for znode
    var path = "/MyFirstZnode"
    var data = "My first zookeeper app"
    println(zk)
    println("Creating ZKPath...")
    
    // Checking if znode exists
    var stat : Stat = zk.exists(path, true)
    println("Checking ZNode exists...")
    
    if (stat != null) {
        println("Node exists and the node version is " + stat.getVersion())
    }
    else {
        println("Node does not exists")
        zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) // Creating znode
        println("Creating ZNode in one path...")
    }
    
    // Input for interaction with Zookeeper Server
    var option = "0"
    while (option != "7" && option.toLowerCase != "exit") {
        print("OPTIONS: \n 1. Create ZNode \n 2. Check ZNode \n 3. Get Data \n 4. Set Data \n 5. Delete ZNode \n 6. Get Children \n 7. Exit \n")
        println("Enter your option for Zookeeper: ")
        option = readLine()
        
        if (option == "1" || option.toLowerCase == "create znode") { // Create ZNode
            println("Enter ZKPath: ")
            path = readLine()
            stat = zk.exists('/' + path, true)
            
            if (stat != null) {
                println("Node already exists: /" + path)
            }
            else {
                try {
                    println("Enter data for ZKPath: ")
                    data = readLine()
                    zk.create('/' + path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
                    println("Creating ZNode in one path...")
                }
                catch {
                    case e: KeeperException => println("Some node in path does not exists")
                }
            }
            Thread.sleep(2000) // wait for 2 seconds
        }
        else if (option == "2" || option == "check znode") { // Check ZNode
            println("Enter ZKPath: ")
            path = readLine()
            stat = zk.exists('/' + path, true)
            println("Checking ZNode exists...")
    
            if (stat != null) {
                println("Node exists and the node version is " + stat.getVersion())
            }
            else {
                println("Node does not exists")
            }
            Thread.sleep(3000) // wait for 3 seconds
        }
        else if (option == "3" || option.toLowerCase == "get data") { // Get Data
            println("Enter ZKPath: ")
            path = readLine()
            try { 
                data = new String(zk.getData('/' + path, false, null), "UTF-8")
                println("Getting ZNode data...")
                println(data)
            }
            catch {
                case e: KeeperException => println("Node does not exists")
            }
            Thread.sleep(4000) // wait for 4 seconds
        }
        else if (option == "4" || option.toLowerCase == "set data") { // Set Data
            println("Enter ZKPath: ")
            path = readLine()
            stat = zk.exists('/' + path, true)
            try {
                println("Enter new data for ZKPath: ")
                data = readLine()                
                zk.setData('/' + path, data.getBytes(), stat.getVersion())
                println("Setting ZNode data...")
            }
            catch {
                case e: KeeperException => println("Node does not exists")
            }
            Thread.sleep(4000) // wait for 4 seconds
        }
        else if (option == "5" || option.toLowerCase == "delete znode") { // Delete ZNode
            println("Enter ZKPath: ")
            path = readLine()
            stat = zk.exists('/' + path, true)
            
            try {
                if (stat != null) {
                    zk.delete('/' + path, stat.getVersion())
                    println("Deleting ZNode in one path...")
                } 
                else {
                    println("Node does not exists")
                }
            }
            catch {
                case e: KeeperException => println("Node has children, cannot be deleted")
            }
            Thread.sleep(2000) // wait for 2 seconds
        }
        else if (option == "6" || option.toLowerCase == "get children") { // Get Children
            println("For root znode only push ENTER")
            println("Enter ZKPath: ")
            path = readLine()
            try {
                val children = zk.getChildren('/' + path, false)
                for (i <- 0 to (children.size() - 1)) {
                    println(children.get(i))
                }
                println("Getting ZNode children...")
            }
            catch {
                case e: KeeperException => println("Node does not exists")
            }
            Thread.sleep(4000) // wait for 4 seconds
        }
        else {
            if (option != "7" && option.toLowerCase != "exit") {
                println("Option does not exists")
            }
            Thread.sleep(2000) // wait for 2 seconds
            
        }
    }

    zk.close()
    println("Closing Zookeeper Connection...")
}