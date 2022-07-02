package server;

import common.model.GameStatus;
import server.logic.TheMindGame;
import common.model.Player;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.*;

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
        transmitter = new MessageTransmitter(socket, this);
        hand = new ArrayList<>();
        status = GameStatus.NotStarted;
        this.level = 0;
    }

    protected void setGame(String game) {
        this.gameName = game;
    }

    public String returnName() {
        return this.name;
    }

    public MessageTransmitter getTransmitter() {
        return this.transmitter;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public void setTheMindGame(TheMindGame theMindGame) {
        this.theMindGame = theMindGame;
    }

    public TheMindGame getTheMindGame() {
        return this.theMindGame;
    }

    @Override
    public void run() {
        setName();
        Server.joinToGame(this);
        getPlayerNumber();
        registerAndGetToken();
        if (isHost) {
            createNewGame();
        }
        addPlayerToGame();
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

    private void setName() {
        try {
            String message = transmitter.getMessage();
            if (message.contains("name"))
                name = message.split(" ")[1];
        } catch (Exception e) {
            SecureRandom random = new SecureRandom();
            name = String.valueOf(random.nextInt());
        }
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

    private void createNewGame() {
        this.gameName = Server.createGameName();
        Server.gameController.createNewGame(this.AuthToken, this.gameName, this.playerNumber);
        this.theMindGame = Server.gameController.getGameByName(this.gameName);
    }

    protected void addPlayerToGame() {
        boolean joinToGameResult = true;
        String result = Server.gameController.join(this.AuthToken, this.gameName);
        if (result.equals("Game is full")) {
            this.isHost = true;
            createNewGame();
            joinToGameResult = false;
        }
        transmitter.sendMessage("joinToGameResult: " + joinToGameResult);
    }

    private void registerAndGetToken() {
       this.AuthToken = Server.gameController.register(this);
        Server.logger.log("New player's name: " + this.name + " Auth token: " + this.AuthToken);
    }

    private void getStartOrder() {
        String token = this.AuthToken;
        String gameName = this.gameName;
        if (isHost) {
            String message = transmitter.getMessage();
            if (!message.split(" ")[0].equals(this.AuthToken)) getStartOrder();
            if (message.split(" ")[1].equals("start")) {
                Thread thread = new Thread(() -> Server.gameController.startGame(token, gameName));
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
                "  heart cards: " + this.theMindGame.getHeartNumber() +
                "  ninja cards: 2" +
                "\nPlayers' name: " +
                this.theMindGame.getPlayersName().toString();

        this.transmitter.sendMessage(message);

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
                    Server.gameController.getGameByName(this.gameName).play(this.AuthToken, cardNumber);
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
        theMindGame.proposeNinja(this.AuthToken);
    }

    @Override
    public void statusChanged(GameStatus status) {
        if (this.status == GameStatus.NotStarted) {
            this.status = status;
            this.play = new Thread(this::play);
            play.start();
        }
        this.status = status;
        if (status == GameStatus.GameOver ||
        status == GameStatus.Win) {
            if (this.status == GameStatus.GameOver) {
                transmitter.sendMessage("You lost the game!!");
            }
            else {
                transmitter.sendMessage("You won the game.");
            }
            transmitter.sendMessage("Game finished");
            try {
                this.socket.close();
                Server.logger.log("The player connection ended. Auth token: " + this.AuthToken);
            } catch (IOException ignored) {}
            if (this.isHost) {
                Server.gameController.getGames().remove(this.gameName);
                if (this.isHost) {
                    Server.logger.log("Game was removed. Game name: " + this.gameName);
                }
            }
        }
    }

    @Override
    public void notifyPlaysCard(String player, Integer card) {
        if (this.hand.contains(card))
            hand.remove(card);
        String message = "player " + player + " played with card " + card +
                "\nlast played card: " + card +
                "  level card: " + this.level +
                "  heart cards: " + this.theMindGame.getHeartNumber() +
                "  ninja cards: " + this.theMindGame.getNinjaCards();
        transmitter.sendMessage(message);
        sendHand();


        TreeSet<Integer> removedCards = new TreeSet<>();
        for (int cardNumber : this.hand) {
            if (card > cardNumber) {
                removedCards.add(cardNumber);
            }
        }
        for (int cardNumber : removedCards) {
            this.theMindGame.play(this.AuthToken, cardNumber);
        }

    }

    @Override
    public void notifyNinjaPropose(String player) {
        this.transmitter.sendMessage("Player " + player + " proposed ninja card.");
    }

    @Override
    public void notifyNinjaAgreement() {
        if (!this.hand.isEmpty()) {
            int card = Collections.min(this.hand);
            this.theMindGame.play(this.AuthToken, card);
        }
        int ninjaCards = Server.gameController.getGameByName(this.gameName).getNinjaCards();
        String message = "1 ninja card used." +
                "\nninja card number: " + ninjaCards;
        transmitter.sendMessage(message);
    }

    @Override
    public void notifyHeartMissed() {
        transmitter.sendMessage("1 heart missed. Heart card number: " +
                Server.gameController.getGameByName(this.gameName).getHeartNumber());
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
        if (this.hand.isEmpty()) this.level++;
        this.hand.add(card);
        this.transmitter.sendMessage("card: " + card + " level: " + this.level);
    }

    @Override
    public void giveToken(String token) {
        this.AuthToken = token;
    }

    class MessageTransmitter {

        private PrintWriter out;
        private Scanner in;
        private final ClientManagerServerSide client;
        String prevMessage = "";

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
            } catch (Exception ignored) {}
            return message;
        }

        protected void sendMessage(String message) {
            if (!message.equals(this.prevMessage)) {
                this.out.println(message);
                this.out.flush();
            }
            this.prevMessage = message;
        }

        protected void sendMessageToOtherClient(String message) {
            try {
                String name = message.split(" ")[3];
                if (name.charAt(name.length() - 1) == ':') {
                    name = name.substring(0, name.length() - 1);
                }
                List<String> names = this.client.getTheMindGame().getPlayersName();
                if (!names.contains(name)) return;
                String emoji = message.split(" ")[4];
                if (emoji.equals(":D") || emoji.equals("):") || emoji.equals("|:")) {
                    for (ClientManagerServerSide client : Server.clientManagers) {
                        if (client.returnName().equals(name)) {
                            client.getTransmitter().sendMessage
                                    ("message from " + this.client.returnName() + ": " + emoji);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
