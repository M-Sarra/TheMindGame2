package server;

import server.log.ILogger;
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

public class GameServer {
    private enum Status {NotStarted, Started, Stopped}

    private final int port;
    private List<ClientAgent> clients;
    private GameController gameController;
    private ILogger logger;// = new Logger("src/main/java/server/log/log");
    private Status status;

    public GameServer(GameController controller, ILogger logger) {
        this.status = Status.NotStarted;
        this.logger = logger;
        this.port = setPort();
        clients = new ArrayList<>();
        gameController = controller;
    }

    public GameController getGameController() {
        return this.gameController;
    }

    public ILogger getLogger() {
        return this.logger;
    }

    public void start() {
        if (this.status == Status.Started)
            return;
        this.status = Status.Started;
        Thread thread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientAgent clientManager = new ClientAgent(this, socket);
                    clients.add(clientManager);
                    logger.log("New client connected...");
                    new Thread(() -> {
                        clientManager.run();
                    }).start();
                }
            } catch (IOException ignored) {
            }
        });
        thread.start();
    }

    public synchronized void joinToGame(ClientAgent client) {
        //Todo: Fateme
        /*if (gameController.isOpen()) {
            if (client.decideToJoin()) {
                String game = returnAnExistingGame();
                if (!game.equals("Game not found!")) {
                    client.setGame(game);
                    client.setHost1(false);
                    client.setTheMindGame(this.gameController.GetGameByName(game));
                    client.addPlayerToGame();
                }
            }
        }*/
    }

    public String createGameName() {
        SecureRandom random = new SecureRandom();
        String gameName = "default";
        do {
            gameName = String.valueOf(random.nextInt());
        } while (gameController.getGames().contains(gameName));
        return gameName;
    }

    private int setPort() {
        try {
            Properties prop = new Properties();
            FileInputStream ip = new FileInputStream("src/main/java/server/file/server.properties");
            prop.load(ip);
            return Integer.parseInt(prop.getProperty("port"));
        } catch (IOException e) {
            return 8000;
        }
    }

    public void remove(ClientAgent clientAgent) {
        this.clients.remove(clientAgent);
    }

    public TheMindGame GetGameByName(String gameName) {
        return this.gameController.getGameByName(gameName);
    }

    public String CreateNewGame(String token, String gameName, int capacity) {
        return this.gameController.createNewGame(token, gameName, capacity);
    }
}