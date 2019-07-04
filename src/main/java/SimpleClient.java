
/***********************************************************************************************************************
 Copyright (c) Damak Mahdi.
 Github.com/damakmahdi
 damakmahdi2012@gmail.com
 linkedin.com/in/mahdi-damak-400a3b14a/
 **********************************************************************************************************************/

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class SimpleClient {
    public static void main(String[] args) {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();

        try {
            Session session = webSocketContainer.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    try {
                        for(int i=0;i<5;i++){
                            session.getBasicRemote().sendText("test");

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, URI.create("ws://localhost:8080/SimpleServlet_WAR/socket"));

        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}