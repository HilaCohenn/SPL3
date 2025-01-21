package bgu.spl.net.srv;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;


public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connections;
    private ConcurrentHashMap<String,List<Integer>> subscribers;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> subscribersId; // hash map - connectionId, hash map - channel, subscriptionId

    public ConnectionsImpl() {
        connections = new ConcurrentHashMap<>();
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
        if (subscribers.get(channel)!=null && !subscribers.get(channel).isEmpty()) {
            for (Integer connectionId : subscribers.get(channel)) {
                ConnectionHandler<T> connect = connections.get(connectionId);
                if (connect != null) {
                    connect.send(msg);
                }
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        connections.remove(connectionId);
    }

    public void addConnection(int connectionId, ConnectionHandler<T> connectionHandler) {
        connections.put(connectionId, connectionHandler);
    }
    
    public void addSubscriber(String channel, int connectionId){
        if(subscribers.get(channel)==null){
            List<Integer> list = new ArrayList<>();
            list.add(connectionId);
            subscribers.put(channel, list);
        }
        subscribers.get(channel).add(connectionId);
    }

    public void addSubscriberId(String channel, int connectionId, int subscriptionId){
        if(subscribersId.get(connectionId)==null){
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            map.put(channel, subscriptionId);
            subscribersId.put(connectionId, map);
        }
        subscribersId.get(connectionId).put(channel, subscriptionId);
    }

    public  void removeSubscriber(String channel, int connectionId){
        subscribers.get(channel).remove(connectionId);
        subscribersId.get(connectionId).remove(channel);
    }

    public String getChannel (int connectionId, int subscriptionId){
        for (String channel : subscribersId.get(connectionId).keySet()){
            if(subscribersId.get(connectionId).get(channel)==subscriptionId){
                return channel;
            }
        }
        return null;
    }

    public boolean isSubscribed (int connectionId, String channel){
        return subscribers.get(channel).contains(connectionId);
    }

    public String getSubscriptionId(int connectionId, String channel){
        return subscribersId.get(connectionId).get(channel).toString();
    }

    public boolean IsConnected(int connectionId){
        return connections.containsKey(connectionId);
    }

}
