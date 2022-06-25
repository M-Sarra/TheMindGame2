package client;

import client.UI.ConsoleManager;

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class GameManagerClientSide {
    private enum TimeStatus {PLAY, GET_STATUS}
    private final Client client;
    private final MessageTransmitter transmitter;
    private final ConsoleManager consoleManager;
    private Thread messageGetter;
    private Thread messageSender;
    private String message = "";
    private boolean isHost = true;
    private TimeStatus timeStatus;

    public GameManagerClientSide(Socket socket, Client client) {
        this.client = client;
        transmitter = new MessageTransmitter(socket);
        consoleManager = new ConsoleManager();
        this.timeStatus = TimeStatus.GET_STATUS;
    }

    public void startGame() {
        introduceGame();
        setDecisionTime();
        getNameAndBotNo();
        getAuthToken();
        orderToStart();
    }

    private void introduceGame() {
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

    private void getNameAndBotNo() {
        consoleManager.sendMessage("Enter your name:");
        client.setName(consoleManager.getMessage());
        transmitter.sendMessage("name: " + client.getName());
        if (isHost) {
            getBotNumber();
        }
    }

    private void getBotNumber() {
        consoleManager.sendMessage("Enter the number of players (2 - 12):");
        try {
            int playerNumber = Integer.parseInt(consoleManager.getMessage());
            if (playerNumber < 2 || playerNumber > 12) getBotNumber();
            client.setPlayerNumber(playerNumber);
            transmitter.sendMessage("playerNumber: " + String.valueOf(client.getPlayerNumber()));
        } catch (Exception e) {
            getBotNumber();
        }
    }

    private void getAuthToken() {
        String message = transmitter.getMessage();
        try {
            if (message.contains("AuthToken")) {
                client.setAuthToken(message.split(" ")[1]);
                consoleManager.sendMessage("AuthToken: " + client.getAuthToken());
            }
        } catch (NumberFormatException ignored) {}
    }

    private void orderToStart() {
        if (isHost) {
            consoleManager.sendMessage("Whenever you want to start the game, type 'start'.");
            String message = consoleManager.getMessage();
            if (message.equals("start")) {
                transmitter.sendMessage("start");
                start();
            } else {
                orderToStart();
            }
        }
    }

    private void start() {
        do {
            message = transmitter.getMessage();

            messageGetter = new Thread(() -> {
                message = transmitter.getMessage();
                if (message.contains("Game started")) {
                    this.timeStatus = TimeStatus.PLAY;
                    consoleManager.sendMessage(message);
                    consoleManager.sendMessage("To send message to another player use following command:" +
                            "\nmessage to player: message" +
                            "\nWrite player's name instead of player. You just can send :D or ): or |:");
                }
                else {
                    if (message.contains("last played card")) this.timeStatus = TimeStatus.PLAY;
                    if (message.contains("useNinjaCard")) askToUseNinja(message);
                    consoleManager.sendMessage(message);
                }
                messageSender.interrupt();
            });

            messageSender = new Thread(() -> {
                if (timeStatus == TimeStatus.PLAY) {
                    message = consoleManager.getMessage();
                    if (isValidMessage(message)) {
                        this.timeStatus = TimeStatus.GET_STATUS;
                        messageGetter.interrupt();
                        transmitter.sendMessage(message);
                    }
                }
            });

            messageGetter.start();
            messageSender.start();

        } while (message.contains("Game finished"));
    }

    private void askToUseNinja(String message) {
        boolean answer = false;
        try {
            answer = Boolean.parseBoolean(message.split(" ")[1]);
        } catch (Exception ignored) {}
        if (answer) {
            AtomicReference<String> answer1 = new AtomicReference<>("n");
            consoleManager.sendMessage("Do you want to use ninja card? type 'y' or 'n'.");
            Thread ninjaCard = new Thread(() -> {
                String answer2 = consoleManager.getMessage();
                answer1.set(answer2);
            });
            ninjaCard.start();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ninjaCard.interrupt();
                }
            }, 7000);
            if (answer1.equals("y") || answer1.equals("Y")) {
                transmitter.sendMessage("useNinjaCard: true");
            }
            else transmitter.sendMessage("useNinjaCard: false");
        }
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
        int n;
        try {
            n = Integer.parseInt(message);
            this.message = "cardNumber: " + n;
        } catch (NumberFormatException e) {
            consoleManager.sendMessage("Invalid input!");
            return false;
        }
        return n > 0 && n < 100;
    }

}
