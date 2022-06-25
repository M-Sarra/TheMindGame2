package server;

import server.logic.GameStatus;
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
    private final String AuthToken;
    private int playerNumber;
    private final MessageTransmitter transmitter;
    private boolean decisionTime = false;
    private boolean usingNinjaCard;
    private String gameName;
    private List<Integer> hand;
    private GameStatus status;
    private boolean heartMissed = false;

    public ClientManagerServerSide(Socket socket) {
        this.socket = socket;
        transmitter = new MessageTransmitter(socket, this);
        this.AuthToken = setAuthToken();
        hand = new ArrayList<>();
        status = GameStatus.NotStarted;
    }

    private String setAuthToken() {
        String token;
        do {
            SecureRandom random = new SecureRandom();
            token = String.valueOf(Math.abs(random.nextInt()));
        } while (Server.containsToken(token));
        return token;
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public boolean isUsingNinjaCard() {
        return usingNinjaCard;
    }

    public void setUsingNinjaCard(boolean useNinjaCard) {
        this.usingNinjaCard = useNinjaCard;
    }

    protected void setGame(String game) {
        this.gameName = game;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    @Override
    public void run() {
        Server.joinToGame(this);
        getNameAndBotNo();
        if (isHost) {
            this.setGame(String.valueOf(Server.gameController.GetGames().size() + 1));
            Server.gameController.CreateNewGame(this.gameName);
        }
        transmitter.sendMessage(AuthToken);
        addPlayerToGame();
        getStartOrder();
        play();
    }

    protected boolean decideToJoin() {
        decisionTime = true;
        transmitter.sendMessage("true");
        String decision = transmitter.getMessage();
        boolean answer = false;
        try {
            answer = Boolean.parseBoolean(decision);
        } catch (Exception ignored) {
        }
        this.isHost = !answer;
        return answer;
    }

    //TODO : set game's player number
    private void getNameAndBotNo() {
        if (!decisionTime) {
            transmitter.sendMessage("false");
        }
        name = transmitter.getMessage();
        if (isHost) {
            try {
                this.playerNumber = Integer.parseInt(transmitter.getMessage());
            } catch (NumberFormatException e) {
                this.playerNumber = 4;
            }
        }
    }

    private void addPlayerToGame() {
        Server.gameController.Join(this, this.gameName);
    }

    //TODO : start game
    private void getStartOrder() {
        String token = this.AuthToken;
        String gameName = this.gameName;
        if (isHost) {
            String message = transmitter.getMessage();
            if (message.equals("start")) {
                Thread thread = new Thread(() -> Server.gameController.StartGame(token, gameName));
                thread.start();;
            }
            else getStartOrder();
        }
    }

    //TODO : Call this method to ask client to use ninja card
    public void decideToUseNinja() {
        if (this.hand.isEmpty()) {
            transmitter.sendMessage("false");
            setUsingNinjaCard(false);
        }
        else {
            transmitter.sendMessage("true");
            try {
                boolean useNinja = Boolean.parseBoolean(transmitter.getMessage());
                setUsingNinjaCard(useNinja);
            } catch (Exception e) {
                setUsingNinjaCard(false);
            }
        }
    }

    //TODO : Call this method to get cardNumber from client
    public void play() {
        String message = "";
        int time = 0;
        while (this.status != GameStatus.GameOver || this.status != GameStatus.Win) {
            if (this.status == GameStatus.NotStarted) continue;
            if (time == 0) {
                transmitter.sendMessage
                        (Server.gameController.GetGameByName(this.gameName).getPlayersName().toString());
                time++;
            }
            message = transmitter.getMessage();
            if (message.equals("Could not get message!!")) continue;
            if (message.contains("cardNumber")) {
                try {
                    int cardNumber = Integer.parseInt(message);
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

    //TODO
    public void sendGameStatus(String playerName, int lastCard) {
        String message = "Game status:\n";
        message += "heart card number: " +
                Server.gameController.GetGameByName(this.gameName).getHeartNumber() + "\n";
        message += "your hand: " + this.hand.toString();
        message += "last played card: " + lastCard + " by player" + playerName;
        transmitter.sendMessage(message);
        if (this.status != GameStatus.GameOver) decideToUseNinja();
    }

    @Override
    public void StatusChanged(GameStatus status) {
        //send game status to client
        this.status = status;
        if (status == GameStatus.GameOver ||
        status == GameStatus.Win) {
            try {
                this.socket.close();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void NotifyPlaysCard(String player, Integer card) {
        //send last played card to client
        if (this.hand.contains(card))
            hand.remove(card);
        if (card > Collections.min(this.hand)) {
            List<Integer> removedCards = new ArrayList<>();
            for (int cardNumber : this.hand) {
                if (card > cardNumber) removedCards.add(cardNumber);
            }
            this.hand.removeAll(removedCards);
        }
        sendGameStatus(player, card);
    }

    @Override
    public void NotifyNinjaPropose(String player) {
        //Todo: برای اطلاع رسانی نینجاست
    }
    @Override
    public void NotifyNinjaAgreement() {
        //Todo: برای اطلاع رسانی نینجاست
    }

    @Override
    public void NotifyHeartMissed() {
        this.heartMissed = true;
        transmitter.sendMessage("1 heart missed. Heart card number: " +
                Server.gameController.GetGameByName(this.gameName).getHeartNumber());
    }

    @Override
    public String GetName() {
        return this.name;
    }

    @Override
    public void GiveCard(Integer card) {
        //send client's cards duo to this method
        hand.add(card);
    }

    class MessageTransmitter {

        private PrintWriter out;
        private Scanner in;
        private final ClientManagerServerSide client;

        private MessageTransmitter(Socket socket, ClientManagerServerSide client) {
            this.client = client;
            try {
                out = new PrintWriter(socket.getOutputStream());
                in = new Scanner(socket.getInputStream());
            } catch (IOException ignored) {}
        }

        protected String getMessage() {
            try {
                return this.in.nextLine();
            } catch (Exception e) {
                return "Could not get message!!";
            }
        }

        protected void sendMessage(String message) {
            this.out.println(message);
            this.out.flush();
        }

        protected void sendMessageToOtherClient(String message) {
            try {
                String name = message.split(" ")[2];
                List<String> names =Server.gameController.GetGameByName(gameName).getPlayersName();
                if (!names.contains(name)) return;
                String emoji = message.split(" ")[4];
                if (emoji.equals(":D") || emoji.equals("):") || emoji.equals("|:")) {
                    for (ClientManagerServerSide client : Server.clientManagers) {
                        if (client.name.equals(name)) {
                            client.transmitter.sendMessage
                                    ("message from " + this.client.name + " : " + emoji);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
