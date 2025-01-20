package bgu.spl.net.api;
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
                handleConnect(parts[1]);
                break;
            case "DISCONNECT":
                handleDisconnect();
                break;
            case "SEND":
                handleSend(parts[1]);
                break;
            case "SUBSCRIBE":
                handleSubscribe(parts[1]);
                break;
            default:
                System.out.println("Unknown command: " + command);
                // send an ERROR
                break;
        }
    }

    private void handleConnect(String details) {
        // Handle connect logic
    }

    private void handleDisconnect() {
        shouldTerminate = true;
        // Handle disconnect logic
    }

    private void handleSend(String details) {
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

    private void handleSubscribe(String details) {
        // Handle subscribe logic
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
    
}
