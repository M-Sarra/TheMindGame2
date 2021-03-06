package server;

import common.model.GameStatus;
import server.log.Logger;
import server.logic.GameController;
import server.logic.TheMindGame;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server {
    private final int port;
    protected static List<ClientManagerServerSide> clientManagers;
    protected static GameController gameController;
    public static final Logger logger = new Logger("src/main/java/server/log/log");
    private ServerSocket serverSocket;

    public Server() {
        this.port = setPort();
        clientManagers = new ArrayList<>();
        gameController = new GameController(logger);
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientManagerServerSide clientManager = new ClientManagerServerSide(socket);
                clientManagers.add(clientManager);
                logger.log("New client connected...");
                new Thread(clientManager).start();
            }
        } catch (IOException ignored) {}
        finally {
            try {
                this.serverSocket.close();
                for (ClientManagerServerSide clientManager : clientManagers) {
                    if (!clientManager.getSocket().isClosed()) {
                        clientManager.getSocket().close();
                    }
                }
            } catch (IOException ignored) {}
        }
    }

    protected static synchronized void joinToGame(ClientManagerServerSide client) {
        if (isNotStarted()) {
            if (client.decideToJoin()) {
                String game = returnAnExistingGame();
                if (!game.equals("Game not found!")) {
                    client.setGame(game);
                    client.setHost(false);
                    client.setTheMindGame(Server.gameController.getGameByName(game));
                }
            }
        }
    }

    public static String createGameName() {
        SecureRandom random = new SecureRandom();
        String gameName = "default";
        do {
            gameName = String.valueOf(random.nextInt());
        } while (gameController.getGames().contains(gameName));
        return gameName;
    }

    private static String returnAnExistingGame() {
        for (String gameName : gameController.getGames()) {
            TheMindGame game = gameController.getGameByName(gameName);
            if (game.isJoinable()) {
                return gameName;
            }
        }
        return "Game not found";
    }

    private static boolean isNotStarted() {
        for (String game : gameController.getGames()) {
            TheMindGame theMindGame = gameController.getGameByName(game);
            if (theMindGame.getStatus() == GameStatus.NotStarted)
                return true;
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
