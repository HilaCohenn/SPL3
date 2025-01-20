package bgu.spl.net.srv;

import java.util.HashMap;
import java.util.Map;

public class User {
    private boolean isConnected;
    private Map<String, Integer> channels; // Channel name to subscription ID
    private String userName;
    private String password;
    private int connectionId; // Unique connection ID for the user

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.isConnected = false;
        this.channels = new HashMap<>();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public Map<String, Integer> getChannels() {
        return channels;
    }

    public void addChannel(String channel, int subscriptionId) {
        channels.put(channel, subscriptionId);
    }

    public void removeChannel(String channel) {
        channels.remove(channel);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public void subscribe(String channel, int subscriptionId) {
        channels.put(channel, subscriptionId);
    }

    public void unsubscribe(String channel) {
        channels.remove(channel);
    }

    public boolean isSubscribed(String channel) {
        return channels.containsKey(channel);
    }

    public int getSubscriptionId(String channel) {
        return channels.getOrDefault(channel, -1);
    }

    @Override
    public String toString() {
        return "User{" +
                "isConnected=" + isConnected +
                ", channels=" + channels +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", connectionId=" + connectionId +
                '}';
    }
}