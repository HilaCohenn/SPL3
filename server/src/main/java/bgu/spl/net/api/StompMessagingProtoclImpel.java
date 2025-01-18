package bgu.spl.net.api;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;

public class StompMessagingProtoclImpel<T> implements StompMessagingProtocol<T> {
    private boolean shouldTerminate = false;
    private ConnectionsImpl<T> connections;
    private int connectionId;

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connections = (ConnectionsImpl<T>) connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(T message) {
        if(message.equals("disconnect")){//comand = disconnect
            shouldTerminate = true;
        }
        else{
           this.connections.send(connectionId, message);
        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
    
}
