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

public:
StompProtocol();
void processFrame(ClientStompFrame frame);
bool isconnected();
bool shouldTerminate();

};

