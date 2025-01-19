package bgu.spl.net.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import bgu.spl.net.srv.StompFrame;
import java.util.List;


public class MessageEncoderDecoderImpel implements MessageEncoderDecoder<StompFrame> {

    private List<Byte> bytes = new LinkedList<>();
    @Override
    public StompFrame decodeNextByte(byte nextByte) {
        if (nextByte == '\u0000') { // assuming messages are null-terminated
            return popFrame();
        }

        bytes.add(nextByte);
        return null; // not a complete message yet
    }

        private StompFrame popFrame() {
            StringBuilder sb = new StringBuilder();
            for (Byte b : bytes) {
                sb.append((char) b.byteValue());
            }
            bytes.clear();
    
            String result = sb.toString();
    

        String[] parts = result.split("\n\n", 2);
        String[] headersAndCommand = parts[0].split("\n", 2);
        String command = headersAndCommand[0];
        String headersPart = headersAndCommand.length > 1 ? headersAndCommand[1] : "";
        String body = parts.length > 1 ? parts[1] : "";

        Map<String, String> headers = new HashMap<>();
        for (String header : headersPart.split("\n")) {
            String[] keyValue = header.split(":", 2);
            if (keyValue.length == 2) {
                headers.put(keyValue[0], keyValue[1]);
            }
        }
        StompFrame sf = new StompFrame(command, headers, body);
        return sf;
    }

    @Override
    public byte[] encode(StompFrame message){
        return message.toString().getBytes();
    }    

}
