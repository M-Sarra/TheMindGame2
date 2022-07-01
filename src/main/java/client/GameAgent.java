package client;

import client.UI.ConsoleManager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameAgent {
    private enum TimeStatus {PLAY, GET_STATUS, END}
    private final RemoteGamePanel client;
    private final ConsoleManager consoleManager;
    private String message = "";
    private boolean isHost = true;
    private TimeStatus timeStatus;
    private int level;
    private List<Integer> hand;

    public GameAgent( RemoteGamePanel client) {
        this.client = client;
        consoleManager = new ConsoleManager();
        this.timeStatus = TimeStatus.GET_STATUS;
        this.hand = new ArrayList<>();
    }

    public void startGame() {
        theMindGame();
        getName();
        getPlayerNumber();
        getJoiningToGameResult("Start Default");
        introduceGame();
    }

    private void theMindGame() {
        consoleManager.sendMessage("The Mind\n" +
                "The human mind is amazingly powerful. " +
                "It can do things for real that sometimes feel like magic.\n");
    }


    private void getName() {
        consoleManager.sendMessage("Enter your name:");
        client.setName(consoleManager.getMessage());
        this.client.sendMessage("name: " + client.getName());
    }

    private void getPlayerNumber() {
        if (isHost) {
            try {
                consoleManager.sendMessage("GameName: ");
            String gameName = consoleManager.getMessage();
            consoleManager.sendMessage("Enter the number of players (2 - 8):");

                int playerNumber = Integer.parseInt(consoleManager.getMessage());
                if (playerNumber < 2 || playerNumber > 8) getPlayerNumber();

                this.client.sendMessage("CG " + playerNumber+ " "+gameName);
            } catch (Exception e) {
                getPlayerNumber();
            }
        }
    }

    private void getJoiningToGameResult(String message) {
        try {
            if (message.contains("joinToGameResult")) {
                boolean result = Boolean.parseBoolean(message.split(" ")[1]);
                if (!result) {
                    this.isHost = true;
                    consoleManager.sendMessage("All games are full. You are joined to a new game.");
                }
            }
        } catch (Exception ignored) {}
    }

    private void getAuthToken(String message) {
        try {
            if (message.contains("AuthToken")) {
                client.setAuthToken(message.split(" ")[1]);
                consoleManager.sendMessage("AuthToken: " + client.getAuthToken());
                this.client.setAuthToken(this.client.getAuthToken());
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

    private void orderToStart(String gameName) {
        if (isHost) {
            consoleManager.sendMessage("Whenever you want to start the game, type 'start'.");
            String message = consoleManager.getMessage();
            if (message.equals("start")) {
                this.client.sendMessage( "Start "+gameName);
            } else {
                orderToStart(gameName);
            }
        }
    }

    private void start(String message1) {
        if (message1.contains("Game started")) {
            this.timeStatus = TimeStatus.PLAY;
            consoleManager.sendMessage(message1);
        }

        Thread messageGetter = new Thread(() -> {
            String prevMessage = this.message;
            while (timeStatus != TimeStatus.END) {
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
                    this.client.sendMessage(message);
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
