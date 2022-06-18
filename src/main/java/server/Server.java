package server;

import server.logic.GameStatus;
import server.logic.TheMindGame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int port;
    private static List<ClientManager> clientManagers;

    public Server() {
        port = setPort();
        clientManagers = new ArrayList<>();
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

    public static boolean containsToken(String token) {
        for (ClientManager clientManager : clientManagers) {
            if (clientManager.getAuthToken().equals(token)) {
                return true;
            }
        }
        return false;
    }

    //TODO
    private int setPort() {
        //read from file by catch and try
        return 8000;
    }

}
