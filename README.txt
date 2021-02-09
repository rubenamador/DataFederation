README - Data Federation

Requisitos de Instalacion:
	1. Instalar Docker
	2. Instalar contenedores de Zookeeper y Cassandra
	3. Descargar el proyecto
	4. Construir los elementos del cluster con la herramienta SBT

PASO 1: Instalar Docker
	Se ha utilizado la version 3.1.0 de Docker Dekstop
	Enlace de descarga: https://docs.docker.com/docker-for-windows/install/
	
	En mi caso, tuve que utilizar la version para Windows 10 Home
	Enlace de descarga: https://desktop.docker.com/win/stable/Docker%20Desktop%20Installer.exe
	
	Tambien, es necesario instalar el siguiente paquete de actualizacion del kernel Linux
	Enlace de descarga: https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi
	
	Para instalar Docker en Windows, les dejo el siguiente enlace
	Manual de instalacion: https://docs.docker.com/docker-for-windows/install-windows-home/#install-docker-desktop-on-windows-10-home

PASO 2: Instalar contenedores de Zookeeper y Cassandra
	Abrir un terminal y seguir los siguientes pasos:
		1. Instalar contenedor de Zookeeper
			Comandos:
				- docker images
				- docker pull rubenamador/zookeeper_kafka:v2
				- docker images
		2. Instalar contenedor de Cassandra
			Comandos:
				- docker images
				- docker pull rubenamador/hadoop_cassandra:v3.2
				- docker images
	En mi caso, al utilizar Windows, un terminal puede ser CMD o PowerShell

PASO 3: Descargar el proyecto
	Abrir un terminal y seguir los siguientes pasos:
		1. Descargar el proyecto de Github
			Comando: git clone https://github.com/rubenamador/DataFederation.git

PASO 4: Construir los elementos del cluster con la herramienta SBT
		1. Instalar SBT en el primer servidor y compilar
			Comandos:
				- cd ./DataFederation
				- cd akka-cluster-quickstart-scala/akka-server1-quickstart-scala
				- sbt
					o Se abrirá un apartado llamado "sbt:akka-server1-quickstart-scala>"
				- compile
				- exit
		2. Instalar SBT en el segundo servidor y compilar
			Comandos:
				- cd ./DataFederation
				- cd akka-cluster-quickstart-scala/akka-server2-quickstart-scala
				- sbt
					o Se abrirá un apartado llamado "sbt:akka-server2-quickstart-scala>"
				- compile
				- exit
		3. Instalar SBT en el cliente y compilar
			Comandos:
				- cd ./DataFederation
				- cd akka-client-quickstart-scala
				- sbt
					o Se abrirá un apartado llamado "sbt:akka-client-quickstart-scala>"
				- compile
				- exit

Pasos de Uso:
	1. Levantar servidores de Zookeeper y Cassandra
	2. Levantar los servidores del cluster
	3. Levantar el cliente del cluster

PASO 1: Levantar servidores de Zookeeper y Cassandra
	Abrir un terminal y seguir los siguientes pasos:
		1. Levantar servidor de Zookeeper
			Comandos:
				- docker ps
				- docker run -dit -p 2181:2181 rubenamador/zookeeper_kafka:v2
				- docker ps
				- docker exec -it [ps] bash
					o Donde [ps] es el identificador del contenedor con Zookeeper
				- cd /root/apache-zookeeper-3.6.1-bin/
				- bin/zkServer.sh start
					o Para apagar Zookeeper posteriormente, solo hay que escribir "bin/zkServer.sh stop" en la ruta de Zookeeper
				- exit
		2. Levantar servidor de Cassandra
			Comandos:
				- docker ps
				- docker run -dit -p 9042:9042 rubenamador/hadoop_cassandra:v3.2
				- docker ps
				- docker exec -it [ps] bash
					o Donde [ps] es el identificador del contenedor con Cassandra
				- cd /root/apache-cassandra-3.0.19-src/
				- bin/cassandra -p /tmp/cassandra.pid
					o Para apagar Cassandra posteriormente, solo hay que escribir "cat /tmp/cassandra.pid" y hacer "kill [ps]"
						siendo [ps] el numero del proceso de Cassandra
				- exit

PASO 2: Levantar los servidores del cluster
	Abrir un terminal y seguir los siguientes pasos:
		1. Levantar el primer servidor del cluster
			Comandos:
				- cd ./DataFederation
				- cd akka-cluster-quickstart-scala/akka-server1-quickstart-scala
				- sbt
					o Se abrirá un apartado llamado "sbt:akka-server1-quickstart-scala>"
				- compile
				- run
					o Dejar la terminal abierta para mantener el servidor levantado
					o Para apagar el servidor posteriormente, solo hay que escribir "exit"
		2. Levantar el segundo servidor del cluster
			Comandos:
				- cd ./DataFederation
				- cd akka-cluster-quickstart-scala/akka-server2-quickstart-scala
				- sbt
					o Se abrirá un apartado llamado "sbt:akka-server2-quickstart-scala>"
				- compile
				- run
					o Dejar la terminal abierta para mantener el servidor levantado
					o Para apagar el servidor posteriormente, solo hay que escribir "exit"

PASO 3: Levantar el cliente del cluster
	Abrir un terminal y seguir los siguientes pasos:
		1. Levantar el cliente
			Comandos:
				- cd ./DataFederation
				- cd akka-client-quickstart-scala
				- sbt
					o Se abrirá un apartado llamado "sbt:akka-client-quickstart-scala>"
				- compile
				- run
		2. Realizar consultas SQL desde el cliente
			Consultas SQL:
				- CREATE TABLE airlines(year INT, month INT, day INT, dayofweek INT, dep_time INT, crs_dep_time INT, arr_time INT, crs_arr_time INT, carrier STRING, flight_num INT, actual_elapsed_time INT, crs_elapsed_time INT, airtime INT, arrdelay INT, depdelay INT, origin STRING, dest STRING, distance INT, taxi_in INT, taxi_out INT, cancelled INT, cancellation_code STRING, diverted INT, carrier_delay INT, weather_delay INT, nas_delay INT, security_delay INT, late_aircraft_delay INT) USING parquet OPTIONS(path '../data/airlines_parquet/*_data.1.parq')
				- CREATE TABLE origin_airports USING org.apache.spark.sql.cassandra OPTIONS (table 'airports', keyspace 'test', cluster 'Test Cluster', pushdown 'true')
				- SELECT * FROM origin_airports LIMIT 20
				- SELECT count(*) FROM origin_airports
				- SELECT DISTINCT country FROM origin_airports
				- CREATE TABLE dest_airports USING org.apache.spark.sql.cassandra OPTIONS (table 'dest_airports', keyspace 'test', cluster 'Test Cluster', pushdown 'true')
				- SHOW TABLES
				- SELECT origin, airport, city, state, country, dest, dest_airport, dest_city, dest_state, dest_country FROM airlines INNER JOIN origin_airports ON airlines.origin = origin_airports.iata INNER JOIN dest_airports ON airlines.dest = dest_airports.dest_iata LIMIT 5
				- DROP TABLE origin_airports
				- DROP TABLE dest_airports
				- DROP TABLE airlines
				- EXIT

Otra informacion:
	1. Detener contenedores de Docker
		Comando:
			- docker stop [ps]
				o Donde [ps] es el identificador del contenedor
				o Para ver los contenedores detenidos, se utiliza el comando "docker ps -a"
				o Para reiniciar un contenedor detenido, se utiliza el comando "docker start"
	2. Borrar contenedores de Docker
		Comando:
			- docker rm [ps]
				o Donde [ps] es el identificador del contenedor
	3. Eliminar imagenes de contenedores de Docker
		Comandos:
			- docker rmi rubenamador/hadoop_cassandra:v3.2
			- docker rmi rubenamador/zookeeper_kafka:v2
