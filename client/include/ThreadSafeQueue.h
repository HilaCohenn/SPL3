#include <queue>
#include <mutex>
#include <condition_variable>


template <typename T>
class ThreadSafeQueue {
private:
    std::queue<T> queue;
    std::mutex mutex;
    std::condition_variable cv;

public:
    ThreadSafeQueue() {}
    void push(const T& value);

    // remove and return the first element in the queue - blocking
    T pop();

 
    bool empty();
};
