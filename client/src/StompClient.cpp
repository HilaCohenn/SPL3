#include <iostream>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <string>
#include "../include/ConnectionHandler.h"

std::mutex mtx;
std::condition_variable cv;
bool done = false;

void socketReader(ConnectionHandler &connectionHandler) {
    while (!done) {
        std::string response;
        if (!connectionHandler.getLine(response)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            done = true;
            cv.notify_all();
            break;
        }
        std::cout << "Reply: " << response << std::endl;
        if (response == "bye") {
            std::cout << "Exiting...\n" << std::endl;
            done = true;
            cv.notify_all();
            break;
        }
    }
}

int main(int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::thread socketThread(socketReader, std::ref(connectionHandler));

    while (!done) {
        std::string line;
        std::getline(std::cin, line);
        if (!connectionHandler.sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            done = true;
            cv.notify_all();
            break;
        }
    }

    socketThread.join();
    return 0;
}