#include "../include/ClientStompFrame.h"

ClientStompFrame::ClientStompFrame(const std::string &command, const std::unordered_map<std::string, std::string> &headers, const std::string &body)
    : command(command), headers(headers), body(body) {}

std::string ClientStompFrame::getCommand() const {
    return command;
}

void ClientStompFrame::setCommand(const std::string &command) {
    this->command = command;
}

std::unordered_map<std::string, std::string> ClientStompFrame::getHeaders() const {
    return headers;
}

void ClientStompFrame::setHeaders(const std::unordered_map<std::string, std::string> &headers) {
    this->headers = headers;
}

std::string ClientStompFrame::getBody() const {
    return body;
}

void ClientStompFrame::setBody(const std::string &body) {
    this->body = body;
}

std::string ClientStompFrame::toString() const {
    std::ostringstream sb;
    sb << command << "\n";
    for (const auto &header : headers) {
        sb << header.first << ":" << header.second << "\n";
    }
    if (!body.empty()) {
        sb << "\n" << body;
    }
    sb << "\n" << "\u0000"; 
    return sb.str();
}
