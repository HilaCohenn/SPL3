package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.*;
import bgu.spl.net.api.MessageEncoderDecoderImpel;
import bgu.spl.net.api.StompMessagingProtoclImpel;



public class StompServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: StompServer <port> <server type>");
            // wrong args
            return;
        }

        int port = Integer.parseInt(args[0]);
        String serverType = args[1];

        if ("tpc".equalsIgnoreCase(serverType)) {
            Server.threadPerClient(
                    port,
                    () -> new StompMessagingProtoclImpel<>(null), // protocol factory
                    MessageEncoderDecoderImpel::new // message encoder decoder factory
            ).serve();
        } else if ("reactor".equalsIgnoreCase(serverType)) {
            Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    port,
                    () -> new StompMessagingProtoclImpel<>(null), // protocol factory
                    MessageEncoderDecoderImpel::new // message encoder decoder factory
            ).serve();
        } else {
            System.out.println("Unknown server type: " + serverType);
            // maybe ERROR? to check!
        }
    }
}