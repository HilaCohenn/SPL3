package bgu.spl.net.srv;
import java.util.HashMap;
import java.util.Map;

public class StompFrame {
    private String command;
    private Map<String, String> headers; 
    private String body; //can be empty


    public StompFrame(String command, Map<String, String> headers, String body) {
        this.command = command;
        this.headers = headers != null ? headers : new HashMap<>();
        this.body = body != null ? body : "";
    }

 
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

