package server;

import server.logic.GameController;
import server.logic.TheMindGameUI;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server {
    private final int port;
    protected static List<ClientManagerServerSide> clientManagers;
    protected static GameController gameController;

    public Server() {
        this.port = setPort();
        clientManagers = new ArrayList<>();
        gameController = new GameController(new TheMindGameUI());
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                final Socket socket = serverSocket.accept();
                ClientManagerServerSide clientManager = new ClientManagerServerSide(socket);
                clientManagers.add(clientManager);
                System.out.println("New client connected...");
                new Thread(clientManager).start();
            }
        } catch (IOException ignored) {}
    }

    protected static synchronized void joinToGame(ClientManagerServerSide client) {
        if (gameController.isOpen()) {
            if (client.decideToJoin()) {
                String game = gameController.joinAnExistingGame();
                if (!game.equals("Game not found!")) {
                    client.setGame(game);
                    client.setHost(false);
                }
            }
        }
    }

    public static boolean containsToken(String token) {
        for (ClientManagerServerSide clientManager : clientManagers) {
            if (clientManager.getAuthToken().equals(token)) {
                return true;
            }
        }
        return false;
    }

    private int setPort() {
        try {
            Properties prop = new Properties();
            FileInputStream ip = new FileInputStream("src/main/java/server/file/server.properties");
            prop.load(ip);
            return Integer.parseInt(prop.getProperty("port"));
        } catch (IOException e) {
            return  8000;
        }
    }

}
