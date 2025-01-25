#pragma once
#include <string>
#include <iostream>
#include <queue>
#include <mutex>
#include <condition_variable>
#include "../include/ClientStompFrame.h"


class Keyboard{
    private:
    int subIdGenerator;
    int receiptIdGenerator;
    bool isConnected;

    public:
    Keyboard();
    ~Keyboard();
    void run(std::queue<ClientStompFrame>& sharedQueue);
    }; 
