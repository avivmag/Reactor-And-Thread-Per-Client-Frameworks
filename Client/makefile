CFLAGS:=-c -Wall -Weffc++ -g 
LDFLAGS:=-lboost_thread -lboost_system -lboost_locale 

all: bin/encoder.o client
	g++ -o bin/client bin/encoder.o bin/connectionHandler.o bin/readWrite.o bin/client.o $(LDFLAGS) 
	bin/client 127.0.0.1 4444
client: bin/connectionHandler.o bin/client.o bin/readWrite.o

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/client.o: src/client.cpp
	g++ $(CFLAGS) -o bin/client.o src/client.cpp
	
bin/encoder.o: encoder/encoder.cpp
	g++ $(CFLAGS) -o bin/encoder.o encoder/encoder.cpp
bin/readWrite.o: src/readWrite.cpp
	g++ $(CFLAGS) -o bin/readWrite.o src/readWrite.cpp
.PHONY: clean
clean:
	rm -f bin/*