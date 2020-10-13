package com.ws.socket.pakago;

import com.google.gson.Gson;
import com.ws.socket.WSClient;
import com.ws.socket.utils.ResourceUtils;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class ElectronicScale {
    private static boolean isDisconnect = false;
    private static int count = 0;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    private final static Logger LOGGER = LoggerFactory.getLogger(ElectronicScale.class);

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
        LOGGER.info("Nhan thong tin => "+message);
        DataElectronicScale dataElectronicScale = new Gson().fromJson(message, DataElectronicScale.class);

        if (dataElectronicScale != null && !StringUtils.isAllBlank(dataElectronicScale.getWeight(), dataElectronicScale.getBarCode())) {
            try {
                File file = new File(ResourceUtils.getValue("folderImage") + "\\" + dataElectronicScale.getBarCode() + ".png");

                String imageEncode = Base64.encodeBase64String(FileUtils.readFileToByteArray(file));

                dataElectronicScale.setImage(imageEncode);

                String data = post(ResourceUtils.getValue("serviceApi"), new Gson().toJson(dataElectronicScale));

                LOGGER.info("Du lieu tra ve => " + data);
            } catch (Exception ex) {
                LOGGER.info("Khong ket noi duoc voi he thong Pakago");
            }
        }
    }

    public static void main(String[] args) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = null;
        System.out.print("Connecting");

        while (true) {
            showConnect(isDisconnect, session);

            try {
                if (isDisconnect || session == null) {
                    session = container.connectToServer(WSClient.class, URI.create(ResourceUtils.getValue("websocket")));
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

    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
