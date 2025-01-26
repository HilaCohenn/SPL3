
#include "../include/Keyboard.h"
using namespace std;

    Keyboard::Keyboard():   subIdGenerator(0), receiptIdGenerator(0),isConnected(false),user(""){}

    void split_string(const string &s, char delimiter, vector<string> &tokens) {
    string token;
    stringstream tokenStream(s);
    while (getline(tokenStream, token, delimiter)) {
        tokens.push_back(token);
    }
}
    void Keyboard::run(ThreadSafeQueue<ClientStompFrame>& sharedQueue)
    {
        while(true)
        {
            std::string input;
            std::getline(std::cin, input);
            std::vector<std::string> inputArgs;
            split_string(input, ' ', inputArgs);
            std::string command = inputArgs.at(0);



           if(command=="login") // handle connect
           {
                int requiredArgs = 4;
               if (inputArgs.size() < static_cast<size_t>(requiredArgs)) {
                    std::cout << "Invalid input. Expected at least " << requiredArgs << " arguments." << std::endl;
                    continue;
                }
                std::string host = inputArgs.at(1);
                std::string username = inputArgs.at(2);
                std::string password = inputArgs.at(3);

                ClientStompFrame frame("CONNECT", {{"accept-version", "1.2"}, {"host", host}, {"login", username}, {"passcode", password}}, "");
                this->user = username;
                sharedQueue.push(frame);
           }


           else if(command=="join")
           {
            int requiredArgs = 2;
            if (inputArgs.size() < static_cast<size_t>(requiredArgs)) {
                std::cout << "Invalid input. Expected at least " << requiredArgs << " arguments." << std::endl;
                continue;
            }
            std::string channel = inputArgs.at(1);

            ClientStompFrame frame("SUBSCRIBE", {{"destination", channel}, {"id", std::to_string(subIdGenerator)},{"receipt", std::to_string(receiptIdGenerator)}}, "");
            subIdGenerator++;
            receiptIdGenerator++;
            sharedQueue.push(frame);
           }

           if(command=="exit")
           {
               int requiredArgs = 2;
               if (inputArgs.size() < static_cast<size_t>(requiredArgs)) {
                    std::cout << "Invalid input. Expected at least " << requiredArgs << " arguments." << std::endl;
                    continue;
                }
            std::string channel = inputArgs.at(1);

            ClientStompFrame frame("UNSUBSCRIBE", {{"destination", channel}, {"id", std::to_string(subIdGenerator)},{"receipt", std::to_string(receiptIdGenerator)}}, "");
            subIdGenerator++;
            receiptIdGenerator++;
            sharedQueue.push(frame);
           }

           if(command=="report")
           {
                int requiredArgs = 2;
                if (inputArgs.size() < static_cast<size_t>(requiredArgs)) {
                    std::cout << "Invalid input. Expected at least " << requiredArgs << " arguments." << std::endl;
                    continue;
                }
            std::string filePath = inputArgs.at(1);
            names_and_events reportedEvents = parseEventsFile(filePath);
            for(Event e: reportedEvents.events){
                e.setEventOwnerUser(user);
                std::string eventBody=e.toString();
                ClientStompFrame frame("SEND", {{"destination", reportedEvents.channel_name},{"receipt", std::to_string(receiptIdGenerator)}}, eventBody);
                receiptIdGenerator++;
                sharedQueue.push(frame);
            }
           }

           if(command=="summary")
              {
                int requiredArgs = 4;
               if (inputArgs.size() < static_cast<size_t>(requiredArgs)) {
                    std::cout << "Invalid input. Expected at least " << requiredArgs << " arguments." << std::endl;
                    continue;
                }
                std::string channel = inputArgs.at(1);
                std::string user = inputArgs.at(2);
                std::string filePath = inputArgs.at(3);
                
                ClientStompFrame frame("SUMMARRY", {{"channel", channel},{"user", user},{"filepath",filePath}},"" );
                }

                
              if(command=="logout")
              {
                ClientStompFrame frame("DISCONNECT", {{"receipt", std::to_string(receiptIdGenerator)}}, "");
                receiptIdGenerator++;
                sharedQueue.push(frame);
              }
        }
    }
    Keyboard::~Keyboard() {}
