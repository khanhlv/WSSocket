package com.ws.socket;

import java.net.URI;

import javax.websocket.*;

@ClientEndpoint
public class WSClient {
    private static boolean isDisconnect = false;
    private static int count = 0;

    @OnClose
    public void onClose(Session session) {
        System.out.println(String.format("Close established. session id: %s", session.getId()));
        isDisconnect = true;
        System.out.print("Connecting");
    }

    @OnOpen
    public void onOpen(Session session) {
        isDisconnect = false;
        System.out.println(String.format("\nConnection established. session id: %s", session.getId()));
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received msg: "+message);
    }

    public static void main(String[] args) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = null;
        System.out.print("Connecting");

        while (true) {
            showConnect(isDisconnect, session);

            try {
                if (isDisconnect || session == null) {
                    session = container.connectToServer(WSClient.class, URI.create("ws://localhost:8080/ws/rate"));
                }

                Thread.sleep(200);
            } catch (Exception ex) {
                closeSession(session);
                session = null;
                isDisconnect = true;
            }
        }
    }

    private static void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception ex) {
            }
        }
    }

    private static void showConnect(boolean isDisconnect, Session session) {
        if (isDisconnect || session == null) {
            count++;

            if (count == 80) {
                System.out.print("\n");
                count = 0;
            }

            System.out.print(".");
        }
    }
}
