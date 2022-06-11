package client;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private String host;
    private int port;
    private String name;
    private int botNumber;
    private int AuthToken;

    public void setName(String name) {
        this.name = name;
    }

    public void setBotNumber(int botNumber) {
        this.botNumber = botNumber;
    }

    public void setAuthToken(int authToken) {
        AuthToken = authToken;
    }

    public String getName() {
        return name;
    }

    public int getBotNumber() {
        return botNumber;
    }

    public int getAuthToken() {
        return AuthToken;
    }

    public void start() {
        setHostAndPort();
        try {
            Socket socket = new Socket(host, port);
            GameManager gameManager = new GameManager(socket, this);
            gameManager.startGame();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //TODO
    private void setHostAndPort() {
        //read from file by catch and try
        host = "localhost";
        port = 8000;
    }
}
