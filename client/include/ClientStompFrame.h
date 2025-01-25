#pragma once
#include <string>
#include <unordered_map>
#include <sstream>

class ClientStompFrame {
private:
    std::string command;
    std::unordered_map<std::string, std::string> headers;
    std::string body;

public:
    ClientStompFrame(const std::string &command, const std::unordered_map<std::string, std::string> &headers, const std::string &body);
    ClientStompFrame(const std::string framestring);
    std::string getCommand() const;
    void setCommand(const std::string &command);

    std::unordered_map<std::string, std::string> getHeaders() const;
    void setHeaders(const std::unordered_map<std::string, std::string> &headers);

    std::string getBody() const;
    void setBody(const std::string &body);

    std::string toString() const;

};