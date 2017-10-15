#ifndef READ_WRITE__
#define READ_WRITE__
#include "../include/connectionHandler.h"
#include <string>
#include <iostream>
#include <stdlib.h>
#include <boost/locale.hpp>
#include <exception>
#include <stdexcept>
#include <sstream>
#include <locale>
/**
 * Holds the methods to call when asking for writing message and recieving one.
 */
class ReadWrite{

private:
  ConnectionHandler * _connectionHandler;
  
public:
  ReadWrite(ConnectionHandler *connectionHandler);
  void recieveMessages();
  void sendMessages();
};


#endif
