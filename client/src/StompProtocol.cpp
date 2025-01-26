
#include "../include/StompProtocol.h"
#include "../include/ConnectionHandler.h"
#include <string>
#include <fstream>

StompProtocol::StompProtocol(std::unordered_map<std::string, ClientStompFrame>& sentFrames,std::unordered_map<int, std::string>& subscriptions): connected(false), terminate(false),sentFrames(sentFrames),subscriptions(subscriptions) {}

void StompProtocol::processFrame(ClientStompFrame frame){
    if(frame.getCommand() == "CONNECTED"){
        connected = true;
        std::cout << "Login successful" << std::endl;
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

std::string epochToDate(int epochTime) {
    std::tm* timeinfo = std::localtime((time_t*)&epochTime);
    char buffer[20];
    std::strftime(buffer, sizeof(buffer), "%d/%m/%y %H:%M", timeinfo);
    return std::string(buffer);
}

void StompProtocol::generateSummary(std::string channel_name, std::string user,std::string filepath)
{
     if (eventsPerChannel.find(channel_name) == eventsPerChannel.end()) {
        std::cout << "No events found for channel: " << channel_name << std::endl;
        return;
    }
    auto& events = eventsPerChannel[channel_name];
    std::vector<Event> userEvents;
    std::copy_if(events.begin(), events.end(), std::back_inserter(userEvents), [&user](const Event& event) {
        return event.getEventOwnerUser() == user;
    });

    if (userEvents.empty()) {
        std::cout << "No events found for user: " << user << " in channel: " << channel_name << std::endl;
        return;
    }

std::sort(userEvents.begin(), userEvents.end(), [](const Event& a, const Event& b) {
        if (a.get_date_time() != b.get_date_time())
            return a.get_date_time() < b.get_date_time(); 
        return a.get_name() < b.get_name();             
    });

    std::ofstream outFile(filepath);
    if (!outFile.is_open()) {
        std::cerr << "Error: Could not open or create file: " << filepath << std::endl;
        return;
    }

    
    outFile << "Channel <" << channel_name << ">\nStats:\n";

    
   int activeCount = 0;
    int forcesArrivalCount = 0;
    for (const auto& event : userEvents) {
        const auto& generalInfo = event.get_general_information();
        if (generalInfo.at("active") == "true") {
            activeCount++;
        }
        if (generalInfo.at("forces_arrival_at_scene") == "true") {
            forcesArrivalCount++;
        }
    }

    outFile << "Total: " << userEvents.size() << "\n";
    outFile << "active: " << activeCount << "\n";
    outFile << "forces arrival at scene: " << forcesArrivalCount << "\n\n";


    outFile << "Event Reports:\n";
    int reportCounter = 1;
    for (const auto& event : userEvents) {
        outFile << "Report_" << reportCounter++ << ":\n";
        outFile << "city: " << event.get_city() << "\n";
        outFile << "date time: " << epochToDate(event.get_date_time()) << "\n";
        outFile << "event name: " << event.get_name() << "\n";

        // Summarize description to 27 characters
        std::string description = event.get_description();
        if (description.size() > 27) {
            description = description.substr(0, 27) + "...";
        }
        outFile << "summary: " << description << "\n\n";
    }

    outFile.close();
    std::cout << "Summary written to " << filepath << std::endl;
}





