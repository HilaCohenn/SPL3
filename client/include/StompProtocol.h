#pragma once
#include <string>
#include <unordered_map>
#include <sstream>
#include "ConnectionHandler.h"
#include "ClientStompFrame.h"
#include "event.h"



// TODO: implement the STOMP protocol
class StompProtocol
{
private:
bool connected;
bool terminate;
std::unordered_map<std::string, std::vector<Event>> eventsPerChannel; 
std::unordered_map<std::string, ClientStompFrame> sentFrames;//saves the frames that were sent and waiting for receipt
std::unordered_map<int, std::string> subscriptions;

public:
StompProtocol(std::unordered_map<std::string, ClientStompFrame>& sentFrames,std::unordered_map<int, std::string>& subscriptions); 
void processFrame(ClientStompFrame frame);
bool isconnected();
bool shouldTerminate();
void generateSummary(std::string channel_name, std::string user,std::string filepath);

};

