#include <stdlib.h>
#include <boost/locale.hpp>
#include <boost/thread.hpp>
#include "../include/connectionHandler.h"
#include "../encoder/utf8.h"
#include "../include/readWrite.h"
#include "../encoder/encoder.h"

/**
* This code starts two new threads which connect to a remote server and sends some message to it, until 'quit' keyword is pressed, then it closed the connection and exit the program.
*/

int main (int argc, char *argv[]) {
    // needed server ip and host.
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    // the ConnectionHandler which handle the connection.
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
	//Encoder encoder;
    ReadWrite mh(&connectionHandler);
    
    // two threads are running, one for recieving message, while the other - for sending messages.
    boost::thread th1(&ReadWrite::recieveMessages, &mh);
    boost::thread th2(&ReadWrite::sendMessages, &mh);
    
    // waits for the threads to finish their work, then exits the program.
    th2.join();
    th1.join();
    return 0;
}
