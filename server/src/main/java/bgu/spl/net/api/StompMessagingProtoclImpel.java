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
        // Handle connect logic
    }

    private void handleDisconnect(StompFrame frame) {
        shouldTerminate = true;
        // Handle disconnect logic
    }

    private void handleSend(StompFrame frame) {
        if(!logedIn){
            // send the client an ERROR frame and then close the connection
            System.out.println("SEND command failed: not logged in");
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
            headers.put("subscription", id);
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

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
    
}
