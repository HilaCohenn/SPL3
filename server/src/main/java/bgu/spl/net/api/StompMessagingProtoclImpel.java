package bgu.spl.net.api;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.StompFrame;


public class StompMessagingProtoclImpel<T> implements StompMessagingProtocol<StompFrame> {
    private boolean shouldTerminate = false;
    private ConnectionsImpl<StompFrame> connections;
    private int connectionId;

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
        String destination = frame.getHeaders().get("destination");
        if (destination == null) {
            //  send the client an ERROR frame and then close the connection
            System.out.println("SEND command missing destination header");
            return;
        }
        boolean isSent = connections.send(destination, frame);
        if (isSent){
            // send the client a RECEIPT frame
        } else {
            // send the client an ERROR frame
        }
        // Handle send logic
    }

    private void handleSubscribe(StompFrame frame) {
        String channel = frame.getHeaders().get("destination");
        int id = Integer.parseInt(frame.getHeaders().get("id"));
        String recipt = frame.getHeaders().get("receipt");
        connections.addSubscriber(channel, connectionId);
        connections.addSubscriberId(channel, connectionId, id);
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
