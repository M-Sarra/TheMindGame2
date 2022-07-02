package server;

import server.logic.GameStatus;
import server.logic.TheMindGame;
import server.logic.model.Player;

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
    public MessageTransmitter transmitter;
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
        getName();
        Server.joinToGame(this);
        if (false){//isHost) {
            this.gameName = Server.createGameName();
            //Todo: Take from message
            int capacity = 4;
            String result = Server.gameController.CreateNewGame(this.AuthToken, this.gameName, capacity);
            if (result.equals("Success")) {
                Server.logger.log("New game created. gameName: " + this.gameName +
                        " Game capacity: " + capacity);
            }
            this.theMindGame = Server.gameController.GetGameByName(this.gameName);
            addPlayerToGame();
        }
        transmitter.sendMessage("AuthToken: " + AuthToken);
        getStartOrder();
    }

    private void getName() {
        try {
            String message = transmitter.getMessage();
            if (message.contains("name"))
                name = message.split(" ")[1];
        } catch (Exception e) {
            SecureRandom random = new SecureRandom();
            name = String.valueOf(random.nextInt());
        }
        Server.logger.log("New player's name: " + this.name + " Auth token: " + this.AuthToken);
    }

    protected void addPlayerToGame() {
        Server.gameController.Join(this.AuthToken, this.gameName);
    }

    private void registerAndGetToken() {
       this.AuthToken = Server.gameController.Register(this);
    }

    private void getStartOrder() {
        String token = this.AuthToken;
        String gameName = this.gameName;
        if (false){//isHost) {
            String message = transmitter.getMessage();
            if (!message.split(" ")[0].equals(this.AuthToken)) getStartOrder();
            if (message.split(" ")[1].equals("start")) {
                Server.logger.log("Game with name " + this.gameName + " started.");
                Thread thread = new Thread(() -> Server.gameController.StartGame(token, gameName));
                thread.start();
            }
            else getStartOrder();
        }
    }

    public void play1() {
        String message;
        message = "Game started" +
                "\nGame name: " + this.gameName +
                "\nlevel card: 1" +
                "\nheart cards: " + Server.gameController.GetGameByName(this.gameName).getHeartNumber() +
                "\nninja cards: 2" +
                "\nPlayer's: " +
                Server.gameController.GetGameByName(this.gameName).getPlayersName().toString();
        transmitter.sendMessage(message);

        while (this.status1 != GameStatus.GameOver || this.status1 != GameStatus.Win) {
            message = transmitter.getMessage();
            if (message.equals("Could not get message!!")) continue;
            if (!message.split(" ")[0].equals(this.AuthToken)) continue;
            if (message.split(" ")[1].equals("0")) useNinjaCard();
            if (message.contains("cardNumber")) {
                try {
                    int cardNumber = Integer.parseInt(message.split(" ")[2]);
                    if (!this.hand.contains(cardNumber) &&
                            Collections.min(this.hand) != cardNumber) return;
                    Server.gameController.GetGameByName(this.gameName).Play(this.AuthToken, cardNumber);
                } catch (Exception ignored) {}
            }
            else if (message.contains("message")) {
                Server.sendMessageToOtherClient(this.name,this.gameName,message);
            }
        }
    }

    public void sendHand() {
        String message = "Your hand: " + this.hand.toString();
        transmitter.sendMessage(message);
    }

    private void useNinjaCard() {
        theMindGame.ProposeNinja(this.AuthToken);
    }

    @Override
    public void StatusChanged(GameStatus status) {
        this.transmitter.sendMessage("Status="+status);
    }

    @Override
    public void NotifyPlaysCard(String player, Integer card) {
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
                "\nlevel card: " + this.theMindGame.GetLevel() +
                "\nheart cards: " + Server.gameController.GetGameByName(this.gameName).getHeartNumber() +
                "\nninja cards: " + Server.gameController.GetGameByName(this.gameName).GetNinjaCards();
        transmitter.sendMessage(message);
        sendHand();
    }

    @Override
    public void NotifyNinjaPropose(String player) {
        //TODO
    }

    @Override
    public void NotifyNinjaAgreement() {
        int ninjaCards = Server.gameController.GetGameByName(this.gameName).GetNinjaCards();
        String message = "1 ninja card used." +
                "\nninja card number: " + ninjaCards;
        transmitter.sendMessage(message);
    }

    @Override
    public void NotifyHeartMissed() {
        transmitter.sendMessage("1 heart missed. Heart card number: " +
                Server.gameController.GetGameByName(this.gameName).getHeartNumber());
    }

    @Override
    public String GetName() {
        return this.name;
    }

    @Override
    public void GiveCard(Integer card) {
        this.transmitter.sendMessage("D="+card);
    }

    @Override
    public void Disconnected() {
        this.play.interrupt();
        Server.remove(this);
        Server.logger.log("Connection is lost!! player's token: " + this.AuthToken);
    }

    @Override
    public void MessageRecived(String message) {
        //Todo: Fateme
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
                this.client.MessageRecived(message);
            } catch (Exception e) {
                this.time++;
                if (this.time > 10) {
                    try {
                        this.socket.close();
                        client.Disconnected();
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
