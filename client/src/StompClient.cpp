#include <iostream>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <string>
#include "../include/ConnectionHandler.h"
#include "../include/ClientStompFrame.h"
#include "../include/ThreadSafeQueue.h"
#include "../include/StompProtocol.h"
#include "../include/Keyboard.h"



int main(int argc, char *argv[]) {
    ThreadSafeQueue<ClientStompFrame> sharedQueue;
    std::unordered_map<std::string, ClientStompFrame> sentFrames;//saves the frames that were sent and waiting for receipt
    std::unordered_map<int, std::string> subscriptions;//saves the channels that the user is subscribed to
    Keyboard keyboard;
    ConnectionHandler* connectionHandler = nullptr;
    StompProtocol protocol = StompProtocol(sentFrames, subscriptions);
    std::thread keyboardThread(&Keyboard::run, &keyboard, std::ref(sharedQueue));
    while(true)
    {
        //send frame to server
        ClientStompFrame frame = sharedQueue.pop();
        if(frame.getCommand() == "CONNECT") {
                if(protocol.isconnected())
                {
                    std::cout << "Client already connected" << std::endl;
                    continue;
                }
            size_t pos = frame.getHeaders().at("host").find(':');
            std::string host = frame.getHeaders().at("host").substr(0, pos);
            short port = std::stoi(frame.getHeaders().at("host").substr(pos + 1));
            connectionHandler = new ConnectionHandler(host, port);
             bool connected = connectionHandler->connect();
              if (!connected) {
            std::cout << "Cannot connect to " << host << ":" << port << std::endl;
        }
        else {
            frame.setHeaders({{"accept-version", "1.2"}, {"host", "stomp.cs.bgu.ac.il"}, {"login", frame.getHeaders().at("login")}, {"passcode", frame.getHeaders().at("passcode")}});
           connectionHandler->sendFrameAscii(frame.toString(), '\0');
        }
        }


        else if(frame.getCommand() == "SEND"||frame.getCommand() == "SUBSCRIBE"||frame.getCommand() == "UNSUBSCRIBE"||frame.getCommand() == "DISCONNECT"){
                        if(!protocol.isconnected())
            {
                std::cout << "user not connected. Please login first" << std::endl;
                continue;
            }
            connectionHandler->sendFrameAscii(frame.toString(), '\0');
            sentFrames[frame.getHeaders().at("receipt")] = frame;
            if(frame.getCommand() == "SUBSCRIBE"){
                subscriptions[std::stoi(frame.getHeaders().at("id"))] = frame.getHeaders().at("destination");
            }
        }

        else if(frame.getCommand() == "SUMMARY"){
            if(!protocol.isconnected())
            {
                std::cout << "user not connected. Please login first" << std::endl;
                continue;
            }
            else if(subscriptions.find(std::stoi(frame.getHeaders().at("id"))) == subscriptions.end())
            {
                std::cout << "user not subscribed to channel" << std::endl;
                continue;
            }
            else{
            std::string channel = frame.getHeaders().at("channel");
            std::string user = frame.getHeaders().at("user");;
            std::string filePath = frame.getHeaders().at("filepath");;
            protocol.generateSummary(channel, user, filePath);
            }
        }


        else if(frame.getCommand() == "DISCONNECT"){
            if(!protocol.isconnected())
            {
                std::cout << "user not connected. Please login first" << std::endl;
                continue;
            }
            connectionHandler->sendFrameAscii(frame.toString(), '\0');
    }

   
    std::string frameString;
    if(!connectionHandler->getFrameAscii(frameString, '\0'))
    protocol.processFrame(ClientStompFrame(frameString));

     if(protocol.shouldTerminate())
      {
        connectionHandler->close();
        delete connectionHandler;
        connectionHandler = nullptr;
        sentFrames.clear();
        subscriptions.clear();
     }

    }
}