package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

public class Client {
    private String host;
    private int port;
    private String name;
    private int playerNumber;
    private String AuthToken;

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public void setAuthToken(String authToken) {
        AuthToken = authToken;
    }

    public String getName() {
        return name;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public void start() {
        setHostAndPort();
        try {
            Socket socket = new Socket(host, port);
            GameManagerClientSide gameManager = new GameManagerClientSide(socket, this);
            gameManager.startGame();
        } catch (IOException e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            start();
        }
    }

    private void setHostAndPort() {
        try {
            Properties prop = new Properties();
            FileInputStream ip = new FileInputStream("src/main/java/client/file/client.properties");
            prop.load(ip);
            host = prop.getProperty("host");
            port = Integer.parseInt(prop.getProperty("port"));
        } catch (IOException e) {
            host = "localhost";
            port = 8000;
        }
    }
}
