#pragma once
#include <string>
#include <iostream>
#include <queue>
#include <mutex>
#include <condition_variable>
#include "../include/ClientStompFrame.h"
#include "../include/ThreadSafeQueue.h"
#include "../include/event.h"

class Keyboard{
    private:
    int subIdGenerator;
    int receiptIdGenerator;
    bool isConnected;
    std::string user;

    public:
    Keyboard();
    ~Keyboard();
    void run(ThreadSafeQueue<ClientStompFrame>& sharedQueue);
    }; 
