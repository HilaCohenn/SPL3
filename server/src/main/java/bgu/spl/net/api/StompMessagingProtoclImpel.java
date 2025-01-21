package bgu.spl.net.api;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.StompFrame;


public class StompMessagingProtoclImpel<T> implements StompMessagingProtocol<StompFrame> {
    private boolean shouldTerminate = false;
    private ConnectionsImpl<StompFrame> connections;
    private int connectionId;
    private int messageCounter = 1;
    private boolean logedIn = false;
    rivate Reactor<T> server;

    public StompMessagingProtoclImpel(Reactor<T> server) {
        this.server = server;
    }

    @Override
    public void start(int connectionId, Connections<StompFrame> connections) {
        this.connections = (ConnectionsImpl<StompFrame>) connections;
        this.connectionId = connectionId;
    }

        @Override
    public void process(StompFrame frame) {
        String command = frame.getCommand();

        switch (command.toUpperCase()) {
            case "CONNECT":
                handleConnect(frame);
                break;
            case "DISCONNECT":
                handleDisconnect(frame);
                break;
            case "SEND":
                handleSend(frame);
                break;
            case "SUBSCRIBE":
                handleSubscribe(frame);
                break;
            case "UNSUBSCRIBE":
                handleUnsubscribe(frame);
                break;
            default:
                System.out.println("Unknown command: " + command);
                // send an ERROR
                break;
        }
    }

    private void handleConnect(StompFrame frame) {
        String userName = frame.getHeaders().get("login");
        String password = frame.getHeaders().get("passcode");

        if (userName == null || password == null) {
            // Send error frame
            return;
        }
        if (logedIn) {
            // Send error frame
            return;
        }

        ConcurrentHashMap<String, User> users = server.getUsers();
        User user = users.get(userName);
        if (user == null) {
            user = new User(userName, password, connectionId);
            server.addUser(userName, user);
        } else if (!user.getPassword().equals(password)) {
            // Send error frame
            return;
        }

        user.setConnected(true);
        connections.addConnection(connectionId, new BlockingConnectionHandler<>(/* parameters */));
        // Send connected frame

    }

    private void handleDisconnect(StompFrame frame) {
        User user = getUserByConnectionId(connectionId);
        if (user != null) {
            user.setConnected(false);
        }
        connections.disconnect(connectionId);
        shouldTerminate = true;
        // Handle disconnect logic
    }

    private void handleSend(StompFrame frame) {
        if(!logedIn){
            // send the client an ERROR frame and then close the connection
            System.out.println("SEND command failed: not logged in");//in error frames
            return;
        }
        String channel = frame.getHeaders().get("destination");
        if (channel == null) {
            //  send the client an ERROR frame and then close the connection
            System.out.println("SEND command missing destination header");
            return;
        }
        String message = frame.getBody();
        if(connections.isSubscribed(connectionId, channel))
        {
            String subId= connections.getSubscriptionId(connectionId, channel);
            String id = Integer.toString(messageCounter);
            this.messageCounter++;
            ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
            headers.put("subscription", subId);
            headers.put("Message-id", id);
            headers.put("destination", channel);
            connections.send(channel, new StompFrame("MESSAGE", headers, message));
            //make sure SEND has recipt header
            String recipt = frame.getHeaders().get("receipt");
            ConcurrentHashMap<String, String> header = new ConcurrentHashMap<>();
            header.put("receipt-id", recipt);
            StompFrame receipt = new StompFrame("RECEIPT", header, "");
            connections.send(connectionId, receipt);
        }
        else{
            // send the client an ERROR frame
            System.out.println("SEND command failed: not subscribed to channel");
        }
     
       
    }

    private void handleSubscribe(StompFrame frame) {
        String channel = frame.getHeaders().get("destination");
        int id = Integer.parseInt(frame.getHeaders().get("id"));
        String recipt = frame.getHeaders().get("receipt");
        //if header missing - send error frame
        connections.addSubscriber(channel, connectionId);
        connections.addSubscriberId(channel, connectionId, id);
        ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
        headers.put("receipt-id", recipt);
        StompFrame receipt = new StompFrame("RECEIPT", headers, "");
        connections.send(connectionId, receipt);
        //send error frsme if needed - when and why?
    }

    private void handleUnsubscribe(StompFrame frame) {
        int id = Integer.parseInt(frame.getHeaders().get("id"));
        String recipt = frame.getHeaders().get("receipt");
        String channel = connections.getChannel(this.connectionId, id);
        if (channel==null){
            // send the client an ERROR frame?
            System.out.println("Unsubscribe failed: not subscribed to channel");
            return;
        }
        connections.removeSubscriber(channel, connectionId);
        ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
        headers.put("receipt-id", recipt);
        StompFrame receipt = new StompFrame("RECEIPT", headers, "");
        connections.send(connectionId, receipt);
    }

    private User getUserByConnectionId(int connectionId) {
        for (User user : server.getUsers().values()) {
            if (user.getConnectionId() == connectionId) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
    
}
