#pragma once
#include "../include/StompProtocol.h"
#include "../include/ConnectionHandler.h"
#include <string>

StompProtocol::StompProtocol(std::unordered_map<std::string, ClientStompFrame>& sentFrames,std::unordered_map<int, std::string>& subscriptions): connected(false), terminate(false),sentFrames(sentFrames),subscriptions(subscriptions) {}

void StompProtocol::processFrame(ClientStompFrame frame){
    if(frame.getCommand() == "CONNECTED"){
        connected = true;
    }
    else if(frame.getCommand() == "RECEIPT"){
        std::string receiptId = frame.getHeaders().at("receipt-id");
        ClientStompFrame frame = sentFrames.at(receiptId);
        sentFrames.erase(receiptId);
        if(frame.getCommand()=="SUBSCRIBE"){
            std::cout << "Joined channel " +frame.getHeaders().at("destination")<< std::endl;
        }
        if(frame.getCommand()=="UNSUBSCRIBE"){
            std::string channel= subscriptions.at(std::stoi(frame.getHeaders().at("id")));
            std::cout << "Exited channel " << channel<< std::endl;
            subscriptions.erase(std::stoi(frame.getHeaders().at("id")));
        }

        if(frame.getCommand()=="DISCONNECT"){ //graceful disconnect?
            terminate = true;
            std::cout << "logout successful" << std::endl;
        }
    }
    else if(frame.getCommand() == "ERROR"){
        terminate = true;
        std::cout << "Error: " << frame.getHeaders().at("message") << std::endl;
        auto receiptIt = frame.getHeaders().find("receipt-id");
        if (receiptIt != frame.getHeaders().end()) {
            std::cout << "Receipt ID: " << frame.getHeaders().at("receipt-id") << std::endl;
        }
        if (!frame.getBody().empty()) {
            std::cout << frame.getBody() << std::endl;
        }
    }
    
    else if(frame.getCommand() == "MESSAGE"){
        std::string channel = frame.getHeaders().at("destination");
        std::string message = frame.getBody();
        Event event = Event(message);
        eventsPerChannel[channel].push_back(event);
    }

}
bool StompProtocol::isconnected(){return connected;}
bool StompProtocol::shouldTerminate(){
   return terminate;}

void StompProtocol::generateSummary(std::string channel_name, std::string user,std::string filepath)
{
    
}



