package test.temp;

import common.model.GameStatus;
import server.logic.TheMindGame;
import common.model.Player;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ClientAgent extends Player implements ISocketListener {
    private String name;
    private String AuthToken;
    private MessageTransmitter transmitter;
    private boolean decisionTime1 = false;
    private String gameName;
    private List<Integer> hand;
    private GameStatus status1;
    private int level1;
    private TheMindGame theMindGame;
    private Thread play;
    private GameServer Server;

    public ClientAgent(GameServer server, Socket socket) {
        this.Server = server;
        registerAndGetToken();
        transmitter = new MessageTransmitter(socket, this);
        hand = new ArrayList<>();
        status1 = GameStatus.NotStarted;
        this.level1 = 0;
    }

    protected void setGame(String game) {
        this.gameName = game;
    }


    public void setTheMindGame(TheMindGame theMindGame) {
        this.theMindGame = theMindGame;
    }

    //@Override
    public void run() {
        setName();
        Server.joinToGame(this);
        if (false){//isHost) {
            this.gameName = Server.createGameName();
            //Todo: Take from message
            int capacity = 4;
            String result = Server.CreateNewGame(this.AuthToken, this.gameName, capacity);
            if (result.equals("Success")) {
                Server.getLogger().log("New game created. gameName: " + this.gameName +
                        " Game capacity: " + capacity);
            }
            this.theMindGame = Server.GetGameByName(this.gameName);
            addPlayerToGame();
        }
        transmitter.sendMessage("AuthToken: " + AuthToken);
        getStartOrder();
    }

    private void setName() {
        try {
            String message = transmitter.getMessage();
            if (message.contains("name"))
                name = message.split(" ")[1];
        } catch (Exception e) {
            SecureRandom random = new SecureRandom();
            name = String.valueOf(random.nextInt());
        }
        Server.getLogger().log("New player's name: " + this.name + " Auth token: " + this.AuthToken);
    }

    protected void addPlayerToGame() {
        Server.getGameController().join(this.AuthToken, this.gameName);
    }

    private void registerAndGetToken() {
       this.AuthToken = Server.getGameController().register(this);
    }

    private void getStartOrder() {
        String token = this.AuthToken;
        String gameName = this.gameName;
        if (false){//isHost) {
            String message = transmitter.getMessage();
            if (!message.split(" ")[0].equals(this.AuthToken)) getStartOrder();
            if (message.split(" ")[1].equals("start")) {
                Server.getLogger().log("Game with name " + this.gameName + " started.");
                Thread thread = new Thread(() -> this. Server.getGameController().startGame(token, gameName));
                thread.start();
            }
            else getStartOrder();
        }
    }

    public void sendHand() {
        String message = "Your hand: " + this.hand.toString();
        transmitter.sendMessage(message);
    }

    @Override
    public void statusChanged(GameStatus status) {
        this.transmitter.sendMessage("Status="+status);
    }

    @Override
    public void notifyPlaysCard(String player, Integer card) {
        if (this.hand.contains(card))
            hand.remove(card);
        if (card > Collections.min(this.hand)) {
            List<Integer> removedCards = new ArrayList<>();
            for (int cardNumber : this.hand) {
                if (card > cardNumber) removedCards.add(cardNumber);
            }
            this.hand.removeAll(removedCards);
        }
        String message = "player " + player + " played with card " + card +
                "\nlast played card: " + card +
                "\nlevel card: " + this.theMindGame.getLevel() +
                "\nheart cards: " + this.theMindGame.getHeartNumber() +
                "\nninja cards: " + this.theMindGame.getNinjaCards();
        transmitter.sendMessage(message);
        sendHand();
    }

    @Override
    public void notifyNinjaPropose(String player) {
    }

    @Override
    public void notifyNinjaAgreement() {
        int ninjaCards = this.theMindGame.getNinjaCards();
        String message = "1 ninja card used." +
                "\nninja card number: " + ninjaCards;
        transmitter.sendMessage(message);
    }

    @Override
    public void notifyHeartMissed() {
        transmitter.sendMessage("1 heart missed. Heart card number: " +
                this.theMindGame.getHeartNumber());
    }

    @Override
    public void notifyJoin(String player) {

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void giveCard(Integer card) {
        this.transmitter.sendMessage("D="+card);
    }

    @Override
    public void giveToken(String token) {
        this.AuthToken = token;
    }

    @Override
    public void disconnected() {
        this.play.interrupt();
        Server.remove(this);
        Server.getLogger().log("Connection is lost!! player's token: " + this.AuthToken);
    }

    @Override
    public void messageReceived(String message) {
    }

    class MessageTransmitter {

        private PrintWriter out;
        private Scanner in;
        private final ISocketListener client;
        private final Socket socket;
        int time = 0;

        public MessageTransmitter(Socket socket, ISocketListener client) {
            this.client = client;
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream());
                in = new Scanner(socket.getInputStream());
            } catch (IOException ignored) {}
        }

        protected String getMessage() {
            String message = "Could not get message!!";
            try {
                message = this.in.nextLine();
                this.client.messageReceived(message);
            } catch (Exception e) {
                this.time++;
                if (this.time > 10) {
                    try {
                        this.socket.close();
                        client.disconnected();
                        return message;
                    } catch (IOException ignored) {}
                }
                getMessage();
            }
            return message;
        }

        protected void sendMessage(String message) {
            this.out.println(message);
            this.out.flush();
        }
    }
}
