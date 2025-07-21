package prism.server;

import com.corundumstudio.socketio.*;
import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.Executors;

public class SocketServer implements AutoCloseable {

    private SocketIOServer server;

    public SocketServer(PRISMServerConfiguration configuration)  {
        Configuration config = new Configuration();
        config.setPort(configuration.getSocketPort());
        config.setHostname(configuration.getSocketHost());

        server = new SocketIOServer(config);

        boolean excludeSender = true;

        server.addConnectListener(
                (client) -> {
                    System.out.println("Client has Connected!");
                });

        server.addDisconnectListener(
                (client) -> {
                    System.out.println("Client has Disconnected!");
                });

        //Equivalent to server.on()
        server.addEventListener("MESSAGE", Object.class,
                (client, data, ackRequest) -> {
                    //print the data
                    System.out.println("Client said: " + data.toString());
                    if(excludeSender){
                        //socket.broadcast("event", data)
                        server.getBroadcastOperations().sendEvent("MESSAGE", client, data);
                    }else{
                        //server.emit("event", data)
                        server.getBroadcastOperations().sendEvent("MESSAGE", data);
                    }


                });

        server.start();
    }

    @Override
    public void close() throws Exception {
        this.server.stop();
    }

    public void send(String event, Object data) {
        this.server.getBroadcastOperations().sendEvent(event, data);
    }
}
