package bgu.spl.net.api;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.*;

public class StompMessagingProtoclImpel<T> implements StompMessagingProtocol<StompFrame> {
    private boolean shouldTerminate = false;
    private ConnectionsImpl<StompFrame> connections;
    private int connectionId;
    private int messageCounter = 1;
    private boolean logedIn = false;
    private Server<T> server;
   

    public StompMessagingProtoclImpel(Server<T> server) {
        this.server = server;
    }


    public void setServer(Server<T> server) {
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
                ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
                map.put("message: ", "Unknown command: " + command);
                StompFrame error = new StompFrame("ERROR", new ConcurrentHashMap<>(), "");
                connections.send(connectionId, error);
                break;
        }
    }

    private void handleConnect(StompFrame frame) {
        String userName = frame.getHeaders().get("login");
        String password = frame.getHeaders().get("passcode");

        // Debugging statements
        System.out.println("handleConnect: userName=" + userName + ", password=" + password);
        System.out.println("handleConnect: server=" + server);

        if (userName == null )
        {//error
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "Missing login header");
            StringBuilder body = new StringBuilder();
            body.append("The message:\n");
            body.append("----------------\n");
            body.append(frame.toString());
            body.append("----------------\n");
            body.append("doesn't contain the login header, which is required for the CONNECT command.");
            handleError(map, body.toString(), frame);
            return;
        }
        
        if (password == null )
        {//error
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "Missing password header");
            StringBuilder body = new StringBuilder();
            body.append("The message:\n");
            body.append("----------------\n");
            body.append(frame.toString());
            body.append("----------------\n");
            body.append("doesn't contain the password header, which is required for the CONNECT command.");
            handleError(map, body.toString(), frame);
            return;
        }
        if (logedIn) {
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "User already logged in");
            handleError(map, "", frame);
        }

        ConcurrentHashMap<String, User> users = server.getUsers();
        System.out.println("handleConnect: users=" + users);
        User user = users.get(userName);
        if (user == null) {
            user = new User(userName, password, connectionId);
            server.addUser(userName, user);
        } else if (!user.getPassword().equals(password)) {
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "Wrong password");
            handleError(map, null, frame);
            return;
        }

        user.setConnected(true);
        logedIn = true;
        ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
        map.put("version: ", "1.2");
        connections.send(connectionId, new StompFrame("CONNECTED", map, ""));

    }

    private void handleDisconnect(StompFrame frame) {
        if(!logedIn){
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "User not logged in, please login first");
            handleError(map, "", frame);
            return;
        }
        User user = getUserByConnectionId(connectionId);
        if (user != null) {
            user.setConnected(false);
        }
        connections.disconnect(connectionId);
        shouldTerminate = true;
    }

    private void handleSend(StompFrame frame) {
        if(!logedIn){
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "User not logged in, please login first");
            handleError(map, "", frame);
            return;
        }

        String channel = frame.getHeaders().get("destination");
        if (channel == null) {
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "Missing destination header");
            StringBuilder body = new StringBuilder();
            body.append("The message:\n");
            body.append("----------------\n");
            body.append(frame.toString());
            body.append("----------------\n");
            body.append("failed because SEND command missing destination header, which is required.");
            handleError(map, body.toString(), frame);
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
            String recipt = frame.getHeaders().get("receipt");
            if(recipt==null){
                return;
            }
            ConcurrentHashMap<String, String> header = new ConcurrentHashMap<>();
            header.put("receipt-id", recipt);
            StompFrame receipt = new StompFrame("RECEIPT", header, "");
            connections.send(connectionId, receipt);
        }
        else{
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "Missing destination header");
            StringBuilder body = new StringBuilder();
            body.append("The message:\n");
            body.append("----------------\n");
            body.append(frame.toString());
            body.append("----------------\n");
            body.append("failed because SEND command missing destination header, which is required.");
            handleError(map, body.toString(), frame);
            return;
        }
     
       
    }

    private void handleSubscribe(StompFrame frame) {
        if(!logedIn){
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "User not logged in, please login first");
            handleError(map, "", frame);
            return;
        }
        String channel = frame.getHeaders().get("destination");
        int id = Integer.parseInt(frame.getHeaders().get("id"));
        String recipt = frame.getHeaders().get("receipt");
        if (channel==null){
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "Missing destination header");
            StringBuilder body = new StringBuilder();
            body.append("The message:\n");
            body.append("----------------\n");
            body.append(frame.toString());
            body.append("----------------\n");
            body.append("failed because Subscribe command missing destination header, which is required.");
            handleError(map, body.toString(), frame);
            return;
        }
        if(connections.isSubscribed(connectionId, channel)){
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "Already subscribed to channel");
            StringBuilder body = new StringBuilder();
            body.append("The message:\n");
            body.append("----------------\n");
            body.append(frame.toString());
            body.append("----------------\n");
            body.append("Subscribe failed: already subscribed to channel");
            handleError(map, body.toString(), frame);
            return;
        }
        connections.addSubscriber(channel, connectionId);
        connections.addSubscriberId(channel, connectionId, id);
        ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
        headers.put("receipt-id", recipt);
        StompFrame receipt = new StompFrame("RECEIPT", headers, "");
        connections.send(connectionId, receipt);
    }

    private void handleUnsubscribe(StompFrame frame) {
        if(!logedIn){
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "User not logged in, please login first");
            handleError(map, "", frame);
            return;
        }
        int id = Integer.parseInt(frame.getHeaders().get("id"));
        String recipt = frame.getHeaders().get("receipt");
        String channel = connections.getChannel(this.connectionId, id);
        if (channel==null){
            ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
            map.put("message: ", "not subscribed to channel");
            StringBuilder body = new StringBuilder();
            body.append("The message:\n");
            body.append("----------------\n");
            body.append(frame.toString());
            body.append("----------------\n");
            body.append("Unsubscrube failed: not subscribed to channel");
            handleError(map, body.toString(), frame);
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

    private void handleError(Map<String, String> headers, String body,StompFrame frame)
    {
        StompFrame error = new StompFrame("ERROR", headers, body);
        connections.send(connectionId, error);
        handleDisconnect(frame);
    }
}
