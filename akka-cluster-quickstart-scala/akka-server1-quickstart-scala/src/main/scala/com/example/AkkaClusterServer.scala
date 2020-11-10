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

class ZooSparkActor(spark: SparkSession, zk: ZooKeeper) extends Actor {
    def receive = {
        case query if query == "EXIT" =>
            println("Finish the Client Application ...")
        case query if (query.toString.contains("CREATE TABLE") || query.toString.contains("SELECT") || query.toString.contains("DROP TABLE") || query.toString.contains("SHOW TABLES")) =>
            var response = "OK"
            try {
                //Check znodes with tables in Spark Session
                import spark.implicits._
                val tables = spark.catalog.listTables.map(_.name).collect()
                val znodes = zk.getChildren("/mymetadata", false)
                for (i <- 0 to (znodes.size() - 1)) {
                    var exist_table = false
                    for (j <- 0 to (tables.length - 1)) {
                        if (znodes.get(i).toString == tables(j).toString) {
                            exist_table = true
                        }
                    }
                    if (!exist_table) { // if znode exist but table is not in Spark Session -> CREATE TABLE
                        val create_table = new String(zk.getData("/mymetadata/" + znodes.get(i).toString, false, null), "UTF-8")
                        spark.sql(create_table.toString)
                    }
                }
                for (i <- 0 to (tables.length - 1)) {
                    var exist_znode = false
                    for (j <- 0 to (znodes.size() - 1)) {
                        if (tables(i).toString == znodes.get(j).toString) {
                            exist_znode = true
                        }
                    }
                    if (!exist_znode) { // if znode not exist but table is in Spark Session -> DROP TABLE
                        val drop_table = "DROP TABLE " + tables(i).toString
                        spark.sql(drop_table)
                    }
                }
                
                // Execute query in Spark Session
                val result = spark.sql(query.toString)
                result.show()
                
                if (query.toString.contains("SELECT") || query.toString.contains("SHOW TABLES")) {
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
                else if (query.toString.contains("CREATE TABLE")) {                
                    val array = query.toString.split(" ").toArray
                    val table_name = array(2)
                    val path = "/mymetadata/" + table_name
                    zk.create(path, query.toString.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) // Creating znode
                    response = "Table \"" + table_name + "\" created correctly \nEND"
                }
                else if (query.toString.contains("DROP TABLE")) {
                    val array = query.toString.split(" ").toArray
                    val table_name = array(2)
                    val path = "/mymetadata/" + table_name
                    val stat = zk.exists(path, true)
                    zk.delete(path, stat.getVersion()) // Deleting znode
                    response = "Table \"" + table_name + "\" removed correctly \nEND"
                }
            }
            catch
            {
                case e1: ClassNotFoundException => response = "ClassNotFoundException: Couldn't find that query. Wrong arguments "
                case e2: InterruptedException => response = "InterruptedException: Session interrupted. "
                case e3: org.apache.spark.sql.catalyst.parser.ParseException => response = "ParseException: Couldn't understand that query. Wrong syntax "
                case e4: org.apache.spark.sql.AnalysisException => response = "AnalysisException: Table or view already created or not found. "
            }
            sender() ! response
    }
}

object AkkaClusterTest extends App {
    val system = ActorSystem("ClusterSystem")
    
    // start the Spark session
    lazy val spark: SparkSession = SparkSession
        .builder()
        .master("local[*]")
        .appName("Spark SQL basic example")
        .getOrCreate()
        //.config("spark.some.config.option", "some-value")
    
    import spark.implicits._
    import org.apache.spark.sql.types._
    
    // start the Zookeeper connection
    val conn = new ZooKeeperConnection()
    val zk = conn.connect("192.168.99.100") //this host is my docker-machine ip --- you can put another host as "localhost"
    
    // Creating root znode if not exists
    var path = "/mymetadata"
    var data = "Data Federation Zookeeper Server" 
    var stat : Stat = zk.exists(path, true)
    
    if (stat != null) {
        println("Node already exists: " + path)
        val children = zk.getChildren(path, false)
        for (i <- 0 to (children.size() - 1)) {
            val query = new String(zk.getData(path + "/" + children.get(i).toString, false, null), "UTF-8")
            spark.sql(query.toString)
        }
    }
    else {
        zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) // Creating znode
    }
    
    val zoospark_actor = system.actorOf(Props(new ZooSparkActor(spark, zk)), name = "zoospark")
    
    // create the cluster
    val cluster = Cluster(system)
    cluster.join(cluster.selfAddress)
    ClusterClientReceptionist(system).registerService(zoospark_actor)
    
    // stop the cluster
    var message = ""
    while (message != "exit") {
        message = readLine()
    }
    
    // commented-out so you can see all the output
    spark.stop()
    println("Finish the Spark Session ...")
    zk.close()
    println("Finish the Zookeeper Connection ...")
    system.terminate()
}