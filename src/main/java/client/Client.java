package client;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private String host;
    private int port;
    private String name;
    private int playerNumber;
    private int AuthToken;

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public void setAuthToken(int authToken) {
        AuthToken = authToken;
    }

    public String getName() {
        return name;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public int getAuthToken() {
        return AuthToken;
    }

    public void start() {
        setHostAndPort();
        try {
            Socket socket = new Socket(host, port);
            GameManagerClientSide gameManager = new GameManagerClientSide(socket, this);
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
