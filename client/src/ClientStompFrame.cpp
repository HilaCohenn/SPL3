#include "../include/ClientStompFrame.h"
ClientStompFrame::ClientStompFrame():command(""), headers(), body("") {}
ClientStompFrame::ClientStompFrame(const std::string &command, const std::unordered_map<std::string, std::string> &headers, const std::string &body): command(command), headers(headers), body(body) {}

ClientStompFrame::ClientStompFrame(const std::string framestring) {
    std::istringstream iss(framestring);
    std::string line;
    std::getline(iss, line);
    command = line;
    while (std::getline(iss, line) && !line.empty()) {
        size_t pos = line.find(':');
        std::string key = line.substr(0, pos);
        std::string value = line.substr(pos + 1);
        headers[key] = value;
    }
    std::getline(iss, body, '\0');
}

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
