#pragma once
#include "../include/StompProtocol.h"
#include "../include/ConnectionHandler.h"
#include <string>

StompProtocol::StompProtocol(): connected(false), terminate(false) {}

void StompProtocol::processFrame(ClientStompFrame frame){
    if(frame.getCommand() == "CONNECTED"){
        connected = true;
    }
    else if(frame.getCommand() == "RECEIPT"){
    //needs to figure out how to manage a list of sent frames and remove the one that got the receipt, and print when needed
    //handle it in case of discoonect
    }
    else if(frame.getCommand() == "ERROR"){
        terminate = true;
    }
    
    else if(frame.getCommand() == "MESSAGE"){
        std::string channel = frame.getHeaders().at("destination");
        std::string message = frame.getBody();
        std::string eventOwner = frame.getHeaders().at("eventOwner");
        Event event = Event(message);
        event.setEventOwnerUser(eventOwner);
        eventsPerChannel[channel].push_back(event);
    }
    else if(frame.getCommand() == "ERROR"){
        std::cout << "Error: " << frame.getBody() << std::endl;
    }
}
bool StompProtocol::isconnected(){return isconnected;}
bool StompProtocol::shouldTerminate(){
   return terminate;}



