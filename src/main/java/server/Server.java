package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int port;
    private final List<ClientManager> clientManagers;

    public Server() {
        port = setPort();
        this.clientManagers = new ArrayList<>();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                final Socket socket = serverSocket.accept();
                ClientManager clientManager = new ClientManager(socket);
                clientManagers.add(clientManager);
                System.out.println("New client connected...");
                new Thread(clientManager).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO
    private int setPort() {
        //read from file by catch and try
        return 8000;
    }

}
