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

public class ClientManagerServerSide extends Player implements Runnable {
    private final Socket socket;
    private boolean isHost = true;
    private String name;
    private String AuthToken;
    private int playerNumber;
    private final MessageTransmitter transmitter;
    private boolean decisionTime = false;
    private String gameName;
    private List<Integer> hand;
    private GameStatus status;
    private int level;
    private TheMindGame theMindGame;
    private Thread play;

    public ClientManagerServerSide(Socket socket) {
        this.socket = socket;
        registerAndGetToken();
        transmitter = new MessageTransmitter(socket, this);
        hand = new ArrayList<>();
        status = GameStatus.NotStarted;
        this.level = 0;
    }

    protected void setGame(String game) {
        this.gameName = game;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public void setTheMindGame(TheMindGame theMindGame) {
        this.theMindGame = theMindGame;
    }

    @Override
    public void run() {
        getName();
        Server.joinToGame(this);
        getPlayerNumber();
        if (isHost) {
            this.gameName = Server.createGameName();
            String result = Server.gameController.CreateNewGame(this.AuthToken, this.gameName, this.playerNumber);
            if (result.equals("Success")) {
                Server.logger.log("New game created. gameName: " + this.gameName +
                        " Game capacity: " + this.playerNumber);
            }
            this.theMindGame = Server.gameController.GetGameByName(this.gameName);
            addPlayerToGame();
        }
        transmitter.sendMessage("AuthToken: " + AuthToken);
        getStartOrder();
    }

    protected boolean decideToJoin() {
        decisionTime = true;
        transmitter.sendMessage("decisionTime: true");
        String decision = transmitter.getMessage();
        boolean answer = false;
        try {
            if (decision.contains("joinToGame"))
                answer = Boolean.parseBoolean(decision.split(" ")[1]);
        } catch (Exception ignored) {}
        this.isHost = !answer;
        return answer;
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

    private void getPlayerNumber() {
        if (!decisionTime) {
            transmitter.sendMessage("decisionTime: false");
        }
        if (isHost) {
            try {
                String message = transmitter.getMessage();
                if (message.contains("playerNumber"))
                    this.playerNumber = Integer.parseInt(message.split(" ")[1]);
            } catch (NumberFormatException e) {
                this.playerNumber = 4;
            }
        }
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
        if (isHost) {
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

    public void play() {
        String message;
        message = "Game started" +
                "\nGame name: " + this.gameName +
                "\nlevel card: 1" +
                "\nheart cards: " + Server.gameController.GetGameByName(this.gameName).getHeartNumber() +
                "\nninja cards: 2" +
                "\nPlayer's: " +
                Server.gameController.GetGameByName(this.gameName).getPlayersName().toString();
        transmitter.sendMessage(message);

        while (this.status != GameStatus.GameOver || this.status != GameStatus.Win) {
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
                transmitter.sendMessageToOtherClient(message);
            }
        }
    }

    public void sendHand() {
        String message = "Your hand: " + this.hand.toString();
        transmitter.sendMessage(message);
    }

    private void useNinjaCard() {
        theMindGame.setNinjaResult(this.AuthToken, true);
    }

    @Override
    public void StatusChanged(GameStatus status) {
        if (this.status == GameStatus.NotStarted) {
            this.play = new Thread(this::play);
            play.start();
        }
        this.status = status;
        if (status == GameStatus.GameOver ||
        status == GameStatus.Win) {
            if (this.status == GameStatus.GameOver) {
                Server.logger.log("Players lost the game " + this.gameName);
            }
            else {
                Server.logger.log("Players won the game " + this.gameName);
            }
            try {
                this.socket.close();
                Server.logger.log("The player connection ended. Auth token: " + this.AuthToken);
            } catch (IOException ignored) {}
            if (this.isHost) {
                Server.gameController.GetGames().remove(this.gameName);
                Server.logger.log("Game was removed. Game name: " + this.gameName);
            }
        }
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
                "\nlevel card: " + this.level +
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
        if (hand.isEmpty()) this.level++;
        hand.add(card);
        if (hand.size() == this.level) sendHand();
    }

    class MessageTransmitter {

        private PrintWriter out;
        private Scanner in;
        private final ClientManagerServerSide client;
        int time = 0;

        private MessageTransmitter(Socket socket, ClientManagerServerSide client) {
            this.client = client;
            try {
                out = new PrintWriter(socket.getOutputStream());
                in = new Scanner(socket.getInputStream());
            } catch (IOException ignored) {}
        }

        protected String getMessage() {
            String message = "Could not get message!!";
            try {
                message = this.in.nextLine();
            } catch (Exception e) {
                this.time++;
                if (this.time > 10) {
                    try {
                        client.socket.close();
                        client.play.interrupt();
                        Server.clientManagers.remove(client);
                        Server.logger.log("Connection is lost!! player's token: " + client.AuthToken);
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

        protected void sendMessageToOtherClient(String message) {
            try {
                String name = message.split(" ")[3];
                List<String> names =Server.gameController.GetGameByName(gameName).getPlayersName();
                if (!names.contains(name)) return;
                String emoji = message.split(" ")[4];
                if (emoji.equals(":D") || emoji.equals("):") || emoji.equals("|:")) {
                    for (ClientManagerServerSide client : Server.clientManagers) {
                        if (client.name.equals(name)) {
                            client.transmitter.sendMessage
                                    ("message from " + this.client.name + ": " + emoji);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
