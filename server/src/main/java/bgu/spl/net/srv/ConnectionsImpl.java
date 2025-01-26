package bgu.spl.net.srv;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import bgu.spl.net.srv.User;



public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connections= new ConcurrentHashMap<>();//Integer=connectionId
    private ConcurrentHashMap<String,List<Integer>> subscribers = new ConcurrentHashMap<>();//Integer=connectionId
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> subscribersId = new ConcurrentHashMap<>(); // hash map - connectionId, hash map - channel, subscriptionId

    public ConnectionsImpl() {
     //
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> connection = connections.get(connectionId);
        if (connection != null) {
            connection.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg) {
        List<Integer> channelSubscribers = subscribers.get(channel);
        if (channelSubscribers != null) {
            for (Integer connectionId : new CopyOnWriteArrayList<>(channelSubscribers)) { // Create a safe copy
                ConnectionHandler<T> connection = connections.get(connectionId);
                if (connection != null) {
                    connection.send(msg);
                }
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        System.out.println("Disconnecting connectionId: " + connectionId);

        // Remove connection handler
        ConnectionHandler<T> handler = connections.remove(connectionId);
        if (handler == null){
            System.out.println("Connection handler is null");
        }
        if (handler != null) {
            try {
                handler.close();
            } catch (Exception e) {
                System.err.println("Error closing connection handler for connectionId: " + connectionId);
                e.printStackTrace();
            }
        }

    // Unsubscribe from all channels
    ConcurrentHashMap<String, Integer> userSubscriptions = subscribersId.remove(connectionId);
    System.out.println("User subscriptions: " + userSubscriptions);
    if (userSubscriptions != null) {
        for (String channel : userSubscriptions.keySet()) {
            System.out.println("Unsubscribing from channel: " + channel);
            subscribers.computeIfPresent(channel, (ch, channelSubscribers) -> {
                channelSubscribers.removeIf(id -> id.equals(connectionId));
                return channelSubscribers.isEmpty() ? null : channelSubscribers;
            });
        }
    }
}

    public void addConnection(int connectionId, ConnectionHandler<T> connectionHandler) { 
        connections.put(connectionId, connectionHandler);
    }

    public void addUser(String userName, User user) {
        users.put(userName, user);
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }
    
    public void addSubscriber(String channel, int connectionId) {
        subscribers.computeIfAbsent(channel, ch -> new CopyOnWriteArrayList<>()).add(connectionId);
    }

    public void addSubscriberId(String channel, int connectionId, int subscriptionId) {
        subscribersId.computeIfAbsent(connectionId, id -> new ConcurrentHashMap<>()).put(channel, subscriptionId);
    }

public void removeSubscriber(String channel, int connectionId) {
    subscribers.computeIfPresent(channel, (ch, channelSubscribers) -> {
        channelSubscribers.remove(Integer.valueOf(connectionId));
        return channelSubscribers.isEmpty() ? null : channelSubscribers;
    });
    ConcurrentMap<String, Integer> subscriptions = subscribersId.get(connectionId);
    if (subscriptions != null) {
        subscriptions.remove(channel);
    }
}

public String getChannel(int connectionId, int subscriptionId) {
    ConcurrentHashMap<String, Integer> subscriptions = subscribersId.get(connectionId);
    if (subscriptions != null) {
        for (String channel : subscriptions.keySet()) {
            if (subscriptions.get(channel) == subscriptionId) {
                return channel;
            }
        }
    }
    return null;
}

    public boolean isSubscribed (int connectionId, String channel){
        
        if (subscribers.get(channel) == null){
            return false;
        }
        boolean isSubscribed = subscribers.get(channel).contains(connectionId);
        return isSubscribed;
    }

    public String getSubscriptionId(int connectionId, String channel){
        return subscribersId.get(connectionId).get(channel).toString();
    }

    public boolean IsConnected(int connectionId){
        return connections.containsKey(connectionId);
    }

}
