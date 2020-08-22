# Distributed Player Status System (DPSS) using CORBA

Submitted By : Vaibhav Malhotra - 40079373

## Build and Run

All code is written in IntelliJ IDE, Java JDK version 8.

CORBA is not supported after java 8.

### IntelliJ:

- Open the project in folder DPSS
- Setup the SDK
- Add runtime params for all the below classes (-ORBInitialPort 1050 - ORBInitialHost)
- Run: ReplicaManager (Run all server replicas)
- Run: PlayerClient (to launch a player window)
- Run: AdminClient (to launch a Admin window)
- To run multiple clients change the configuration to “Allow parallel run”.


### From Command Line

- Move to **DPSS** directoy: `cd DPSS`.

- Create a new folder named **dist** in the current folder : `mkdir dist`.

- Compile the code (outputting into dist folder): `javac -d dist src/**/*.java`.

- Move to **dist** folder: `cd dist`.

- Run the Replica Manager using following command:

Run ORBD through cmd:
```
start orbd -ORBInitialPort 1050
```
Start Servers: 
```
start java ReplicaManager.ReplicaManager r -ORBInitialPort 1050 - ORBInitialHost localhost
```

- Run the clients (will have to open in different terminals):

```
java PlayerClient -ORBInitialPort 1050 -ORBInitialHost localhost
java AdminClient -ORBInitialPort 1050 -ORBInitialHost localhost
```

## Default ports 
- Replica1 (leader)
    - AmericanServer – 8080
    - EuropeanServer - 8081
    - AsianServer – 8082
- Replica2
    - AmericanServer – 2001
    - EuropeanServer - 2002
    - AsianServer – 2003
- Replica3
    - AmericanServer – 3001
    - EuropeanServer - 3002  
    - AsianServer – 3003

The ports can be changed in Constants.java file.

## Architecture

The below diagram describes the major components of the system. The client(player and admin), the front end, the replicas, the replica manager, and the reliable FIFO UDP.

<img src="https://github.com/Vaibhav3M/AirBnB_Analytics/blob/master/Analysis/Visualizations/SHAP.png" height="500"/> 



The client communicates with the front-end using CORBA invocation, and all the other communications in the system are done using reliable FIFO UDP.
The Front-end is connected with the leader replica with UDP which will be used by Front-end to send client’s requests to the leader replica.
The leader replica sends the client’s request to other follower replicas via reliable UDP FIFO queue. The leaders also communicates with the Replica Manager to update the reposes of other replicas.


## Concepts implemented

### 1.	CORBA using Java IDL
CORBA is used to a design specification for an Object Request Broker (ORB). This ORB provides the mechanism required for distributed objects to communicate and invoke server methods.

### 2. Reliable FIFO UDP
The UDP is used for the communications between FE, leader, RM and other replicas. This communication is made reliable and FIFO in order to avoid message loss and guarantee correctness.
Please refer to page above for in-depth explanation.
 

### 3.	Multi-threading
-	All servers run on their individual thread
-	All UDP requests are sent on a new thread
- All client requests are sent on a new thread

### 4.	HashTables-DataStructure
Player data on server are stored in a Hashtables. Hashtables are thread-safe and promote concurrency.

### 5. Locks
Lock (ReentrantLock) is used for proper synchronization to allow multiple users to perform operations for the same or different accounts at the same time.


## Detailed report is avilable here [Report](https://github.com/Vaibhav3M/DPSS/blob/master/Project-Report.pdf)

