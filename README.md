# Reactor-And-Thread-Per-Client-Frameworks

This repository is a demonstration of two server-side methodologies - Thread per client and Reactor.
It also includes an implementation of a specific protocol over it - the text based game protocol (TBGP), an implementation of the bluffer game which uses the framework and lastly, a client which can connect to the server over TBGP and play the available games.

The development of this framework was done in pairs as part of an assignment in "Systems Programming" course at Ben-Gurion University in the beginning of 2016.

A detailed description of the framework and the implementation and the Bluffer game can be found in the assignment desciption attached - section 2, 3 and 4 and on the links below.

## Thread per client vs Reactor

Both methodologies share the same purpose - supply a capability in which multiple clients can connect to one server and interact with each other.
There is a lot of difference between Thread-per-client and Reactor methodologies.
The biggest one lies in the answer to the 'How?' question. How can we achieve our main purpose?
Thread-per-client generates a thread for every client connecting to the server. The thread 'dies' only when the client's connection is terminated.
On the contrary, When being created the Reactor initiates a fixed size pool of threads, and assign a thread for jobs which are created by incoming client data. When the thread finishes the assigned job, he then returns to the pool and waits for a new job's assignment.

## Tokenizer

Both methodologies uses the same approach to recieve full message, they both use the tokenizer pattern.
When sending/receiving data over sockets to/from the network, the data is windowed to the end-to-end network capabilities. And so, it is highly likely the server or the client will recieve only partial message when data segment is received.
The role of the tokenizer is to either split text into terms whenever it matches a word separator, or to capture matching text as terms.

## Java Concurrency and Synchronization
This assignment is a great example for handling threads and the shared memory between them.

Threads can be powerful yet unpredictable. When dealing with threads, one must always act cautiously.
In this assignment we've made sure that side effects from parallelizing threads would never occur, all the while trying to reach the best performance available.

## Callback
Another notable mechanism we used is callback. We used callbacks to send responds to the client for a message the server received.

## Getting Started
### Prerequisites

For the server side:
1. Java SE Runtime Environment 8 (at least), can be found: 
	http://www.oracle.com/technetwork/java/javase/downloads/index.html
	
For the Client side:
1. Kubuntu - this program was tested only on kubuntu, but it probably can be ran on any other known gcc compatible operating systems.
	https://kubuntu.org/getkubuntu/</br>
(The following are for those who which to compile the files themselves)
2. GNU make
	https://www.gnu.org/software/make/
3. g++ compiler
	via ```sudo apt-get install g++``` on ubuntu based os (kubuntu included).
4. boost library
	via ```sudo apt-get install libboost-all-dev``` on ubuntu based os (kubuntu included).
	
Note: this is how I used to build and run the program. There are many other well-known compilers to compile this c++ files for other types of operating systems.

### Running the Bluffer game	
#### Running the server

To run the server, choose first the desired methodology: reactor or thread per client

##### Reactor

From Terminal/cmd type:
```
java -jar path_of_clone/server/reactor.jar <server_port_number> <pool_size>
```
<server_port_number> - number of port the server needs to wait for incomming connections.</br>
<pool_size> - reactor's thread pool size.

##### Thread per client
From Terminal/cmd type:
```
java -jar path_of_clone/server/thread_per_client.jar <server_port_number>
```
<server_port_number> - number of port the server needs to wait for incomming connections.

#### Running the client

1. open terminal and navigate to the program directory
2. type `./Client/bin/client <server_ip_address> <server_port>` and press enter.
3. enjoy :D.</br>
Note: always run the client after server is ready to serve.

## Built With

### Server
* [Maven](https://maven.apache.org/) - Software project management which manage project's build.
* [Gson](https://github.com/google/gson) - A Java serialization/deserialization library to convert Java Objects into JSON and back.

### Client
* [GNU make](https://www.gnu.org/software/make/) - A framework used for simple code compilation.
* [gcc](https://gcc.gnu.org/) - The compiler itself.
* [boost library](http://www.boost.org/) - Used known already implemented data structures.

## Useful links

* The original source of the assignment: https://www.cs.bgu.ac.il/~spl161/wiki.files/assignment3[2].pdf.
* https://en.wikipedia.org/wiki/Thread_(computing)
* https://en.wikipedia.org/wiki/Callback_(computer_programming)
* https://en.wikipedia.org/wiki/Lexical_analysis#Tokenization
* https://en.wikipedia.org/wiki/Reactor_pattern
* https://en.wikipedia.org/wiki/Network_socket
