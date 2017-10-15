#include "../include/readWrite.h" 


ReadWrite::ReadWrite(ConnectionHandler *connectionHandler) : _connectionHandler(connectionHandler){}
void ReadWrite::recieveMessages(){
	while(1){
		std::string answer;
        if (!_connectionHandler->getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
		int len=answer.length();
		// A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
		// we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
	answer.resize(len-1);
        std::cout << answer << std::endl << std::endl;
	
	std::locale loc;
	for (std::string::size_type i=0; i<answer.length(); ++i)
	  answer[i] = std::tolower(answer[i],loc);
	
        if (answer == "sysmsg quit accepted")
            break;
  }
}
void ReadWrite::sendMessages(){
	while (1) {
	      const short bufsize = 1024;
	      char buf[bufsize];
	      std::cin.getline(buf, bufsize);
	      std::string line(buf);
	      
	      _connectionHandler->sendLine(line);
	      
	      std::locale loc;
	      for (std::string::size_type i=0; i<line.length(); ++i)
		line[i] = std::tolower(line[i],loc);
	      
	      if(line == "quit")
		  break;
	}
}
