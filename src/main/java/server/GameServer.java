package server;

import server.log.ConsoleLogger;
import server.log.ILogger;
import server.log.Logger;
import server.logic.GameController;
import server.logic.GameStatus;
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
    private final int port;
    private List<ClientAgent> clients;
    public GameController gameController;
    public ILogger logger;// = new Logger("src/main/java/server/log/log");

    public GameServer(GameController controller,ILogger logger) {
        this.logger = logger;
        this.port = setPort();
        clients = new ArrayList<>();
        gameController = controller;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                final Socket socket = serverSocket.accept();
                ClientAgent clientManager = new ClientAgent(this,socket);
                clients.add(clientManager);
                logger.log("New client connected...");
                new Thread(()->{clientManager.run();}).start();
            }
        } catch (IOException ignored) {}
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
        } while (gameController.GetGames().contains(gameName));
        return gameName;
    }

    private String returnAnExistingGame() {
        for (String gameName : gameController.GetGames()) {
            TheMindGame game = gameController.GetGameByName(gameName);
            if (game.IsJoinable()) {
                    return gameName;
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

    public void remove(ClientAgent clientAgent) {
        this.clients.remove(clientAgent);
    }

    protected void sendMessageToOtherClient(String clientName,String gameName, String message) {
        try {
            String name = message.split(" ")[3];
            List<String> names =Server.gameController.GetGameByName(gameName).getPlayersName();
            if (!names.contains(name)) return;
            String emoji = message.split(" ")[4];
            if (emoji.equals(":D") || emoji.equals("):") || emoji.equals("|:")) {
                for (ClientAgent client : this.clients) {
                    if (clientName.equals(name)) {
                        client.transmitter.sendMessage
                                ("message from " + clientName + ": " + emoji);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

}
