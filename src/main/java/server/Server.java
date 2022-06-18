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
    public static List<TheMindGame> games;

    public Server() {
        port = setPort();
        clientManagers = new ArrayList<>();
        games = new ArrayList<>();
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

    protected static synchronized void setGame(ClientManager client) {
        /*TheMindGame clientGame = null;
        boolean wantsNewGame = true;
        for (TheMindGame game : games) {
            if (game.getStatus() == GameStatus.NotStarted &&
                    game.getClientManagersNumber() < game.getPlayerNumber()) {
                if (client.decideToPLay()) {
                    game.addClientManager(client);
                    clientGame = game;
                    wantsNewGame = false;
                }
                break;
            }
        }
        if (wantsNewGame) {
            TheMindGame newGame = new TheMindGame();
            newGame.addClientManager(client);
            newGame.setHost(client);
            games.add(newGame);
            clientGame = newGame;
        }
        client.getGame(clientGame);*/
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
