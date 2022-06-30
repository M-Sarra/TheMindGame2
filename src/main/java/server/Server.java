package server;

import server.log.Logger;
import server.logic.GameController;
import server.logic.GameStatus;
import server.logic.TheMindGame;
import server.logic.TheMindGameUI;

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
                logger.log("New client connected...");
                new Thread(clientManager).start();
            }
        } catch (IOException ignored) {}
    }

    protected static synchronized void joinToGame(ClientManagerServerSide client) {
        if (gameController.isOpen()) {
            if (client.decideToJoin()) {
                String game = returnAnExistingGame();
                if (!game.equals("Game not found!")) {
                    client.setGame(game);
                    client.setHost(false);
                    client.setTheMindGame(Server.gameController.GetGameByName(game));
                }
            }
        }
    }

    public static String createGameName() {
        SecureRandom random = new SecureRandom();
        String gameName = "default";
        do {
            gameName = String.valueOf(random.nextInt());
        } while (gameController.GetGames().contains(gameName));
        return gameName;
    }

    private static String returnAnExistingGame() {
        for (String gameName : gameController.GetGames()) {
            TheMindGame game = gameController.GetGameByName(gameName);
            if (game.getStatus() == GameStatus.NotStarted) {
                if (game.capacity < game.GetCountOfPlayers()) {
                    return gameName;
                }
            }
        }
        return "Game not found";
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
