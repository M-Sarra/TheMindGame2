package client;

import client.UI.ConsoleManager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameManagerClientSide {
    private enum TimeStatus {PLAY, GET_STATUS, END}
    private final Client client;
    private final MessageTransmitter transmitter;
    private final ConsoleManager consoleManager;
    private String message = "";
    private boolean isHost = true;
    private TimeStatus timeStatus;
    private int level;
    private List<Integer> hand;

    public GameManagerClientSide(Socket socket, Client client) {
        this.client = client;
        transmitter = new MessageTransmitter(socket);
        consoleManager = new ConsoleManager();
        this.timeStatus = TimeStatus.GET_STATUS;
        this.hand = new ArrayList<>();
    }

    public void startGame() {
        theMindGame();
        getName();
        setDecisionTime();
        getPlayerNumber();
        getJoiningToGameResult();
        getAuthToken();
        introduceGame();
        orderToStart();
    }

    private void theMindGame() {
        consoleManager.sendMessage("The Mind\n" +
                "The human mind is amazingly powerful. " +
                "It can do things for real that sometimes feel like magic.\n");
    }

    private void setDecisionTime() {
        boolean decisionTime = false;
        try {
            String message = transmitter.getMessage();
            if (message.contains("decisionTime"))
                decisionTime = Boolean.parseBoolean(message.split(" ")[1]);
        } catch (Exception ignored) {}
        if (decisionTime) {
            consoleManager.sendMessage("There is a 'not started' game. Do you want to join?\n" +
                    "type y or n.");
            String answer = consoleManager.getMessage();
            while (true) {
                if (answer.equals("y") || answer.equals("Y")) {
                    transmitter.sendMessage("joinToGame: true");
                    isHost = false;
                    break;
                } else if (answer.equals("n") || answer.equals("N")) {
                    transmitter.sendMessage("joinToGame: false");
                    break;
                }
                else {
                    consoleManager.sendMessage("Invalid input!");
                }
            }
        }
    }

    private void getName() {
        consoleManager.sendMessage("Enter your name:");
        client.setName(consoleManager.getMessage());
        transmitter.sendMessage("name: " + client.getName());
    }

    private void getPlayerNumber() {
        if (isHost) {
            consoleManager.sendMessage("Enter the number of players (2 - 8):");
            try {
                int playerNumber = Integer.parseInt(consoleManager.getMessage());
                if (playerNumber < 2 || playerNumber > 8) getPlayerNumber();
                client.setPlayerNumber(playerNumber);
                transmitter.sendMessage("playerNumber: " + client.getPlayerNumber());
            } catch (Exception e) {
                getPlayerNumber();
            }
        }
    }

    private void getJoiningToGameResult() {
        try {
            String message = transmitter.getMessage();
            if (message.contains("joinToGameResult")) {
                boolean result = Boolean.parseBoolean(message.split(" ")[1]);
                if (!result) {
                    this.isHost = true;
                    consoleManager.sendMessage("All games are full. You are joined to a new game.");
                }
            }
        } catch (Exception ignored) {}
    }

    private void getAuthToken() {
        String message = transmitter.getMessage();
        try {
            if (message.contains("AuthToken")) {
                client.setAuthToken(message.split(" ")[1]);
                consoleManager.sendMessage("AuthToken: " + client.getAuthToken());
                transmitter.setAuthToken(this.client.getAuthToken());
            }
        } catch (NumberFormatException ignored) {}
    }

    private void introduceGame() {
        consoleManager.sendMessage("After the game starts, the names of the players will be sent to you." +
                "\nTo send message to another player use following command:" +
                "\nmessage to player: message" +
                "\nWrite player's name instead of player. You just can send :D or ): or |:" +
                "\nGame has 2 ninja cards at first. Enter 0 whenever you want to use the ninja card");
    }

    private void orderToStart() {
        if (isHost) {
            consoleManager.sendMessage("Whenever you want to start the game, type 'start'.");
            String message = consoleManager.getMessage();
            if (message.equals("start")) {
                transmitter.sendMessageByToken("start");
                start();
            } else {
                orderToStart();
            }
        }
    }

    private void start() {
        message = transmitter.getMessage();
        if (message.contains("Game started")) {
            this.timeStatus = TimeStatus.PLAY;
            consoleManager.sendMessage(message);
        }

        Thread messageGetter = new Thread(() -> {
            String prevMessage = this.message;
            while (timeStatus != TimeStatus.END) {
                message = transmitter.getMessage();
                if (message.equals("Could not get message from server!!")) {
                    consoleManager.sendMessage("The connection to the server was lost!");
                    System.exit(0);
                }

                if (message.equals(prevMessage) && !message.contains("message")) continue;

                if (message.split(" ")[0].equals("card:")) {
                    try {
                        this.level = Integer.parseInt(message.split(" ")[3]);
                        int card = Integer.parseInt(message.split(" ")[1]);
                        if (this.hand.size() >= this.level) {
                            this.hand.clear();
                        }
                        if (!this.hand.contains(card)) this.hand.add(card);
                        if (this.hand.size() == this.level) {
                            this.consoleManager.sendMessage("your hand: " + this.hand);
                        }
                    } catch (Exception ignored) {}
                }
                else {
                    consoleManager.sendMessage(message);
                    if (message.contains("last played card")) this.timeStatus = TimeStatus.PLAY;
                    if (message.contains("Game finished")) {
                        this.timeStatus = TimeStatus.END;
                        System.exit(0);
                    }
                }
                prevMessage = message;
            }
        });

        Thread messageSender = new Thread(() -> {
            while (timeStatus != TimeStatus.END) {
                message = consoleManager.getMessage();
                if (isValidMessage(message)) {
                    if (!message.contains("message")) {
                        this.timeStatus = TimeStatus.GET_STATUS;
                    }
                    transmitter.sendMessageByToken(message);
                }
            }
        });

        messageGetter.start();
        messageSender.start();
    }

    private boolean isValidMessage(String message) {
        if (message.contains("message")) {
            try {
                String emoji = message.split(" ")[3];
                if (emoji.equals(":D") || emoji.equals("):") || emoji.equals("|:")) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        if (timeStatus == TimeStatus.PLAY) {
            int n;
            try {
                n = Integer.parseInt(message);
                if (n != 0) this.message = "cardNumber: " + n;
            } catch (NumberFormatException e) {
                consoleManager.sendMessage("Invalid input!");
                return false;
            }
            return n >= 0 && n < 100;
        }
        return false;
    }

}
