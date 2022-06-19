package client;

import client.UI.ConsoleManager;

import java.net.Socket;

public class GameManagerClientSide {
    private final Client client;
    private final MessageTransmitter transmitter;
    private final ConsoleManager consoleManager;
    private Thread messageGetter;
    private Thread messageSender;
    private String message = "";
    private boolean isHost = true;

    public GameManagerClientSide(Socket socket, Client client) {
        this.client = client;
        transmitter = new MessageTransmitter(socket);
        consoleManager = new ConsoleManager();
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
            decisionTime = Boolean.parseBoolean(transmitter.getMessage());
        } catch (Exception ignored) {}
        if (decisionTime) {
            consoleManager.sendMessage("There is a 'notStarted' game. Do you want to join?\n" +
                    "type y or n.");
            String answer = consoleManager.getMessage();
            while (true) {
                if (answer.equals("y") || answer.equals("Y")) {
                    transmitter.sendMessage("true");
                    isHost = false;
                    break;
                } else if (answer.equals("n") || answer.equals("N")) {
                    transmitter.sendMessage("false");
                    break;
                }
                else {
                    consoleManager.sendMessage("Invalid input!");
                }
            }
        }
    }

    private void getNameAndBotNo() {
        //send message duo to a better way
        consoleManager.sendMessage("Enter your name:");
        client.setName(consoleManager.getMessage());
        transmitter.sendMessage(client.getName());
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
            transmitter.sendMessage(String.valueOf(client.getPlayerNumber()));
        } catch (Exception e) {
            getBotNumber();
        }
    }

    private void getAuthToken() {
        //check if message contains auth token
        String message = transmitter.getMessage();
        try {
            client.setAuthToken(Integer.parseInt(message));
            consoleManager.sendMessage("AuthToken: " + client.getAuthToken());
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
            //ask to use ninja card first of each round
            if (message.equals("true")) {
                consoleManager.sendMessage("Do you want to use ninja card? type 'y' or 'n'.");
                message = consoleManager.getMessage();
                if (message.equals("y") || message.equals("Y")) {
                    transmitter.sendMessage("true");
                }
                else transmitter.sendMessage("false");
            }

            messageGetter = new Thread(() -> {
                message = transmitter.getMessage();
                messageSender.interrupt();
                //deserialize message

                consoleManager.sendMessage(message);
            });

            messageSender = new Thread(() -> {
                message = consoleManager.getMessage();
                //message could be invalid yet
                if (isValidNumber(message)) {
                    messageGetter.interrupt();
                    transmitter.sendMessage(message);
                }
            });

            messageGetter.start();
            messageSender.start();

        } while (message.contains("Game finished"));
    }

    private boolean isValidNumber(String message) {
        int n;
        try {
            n = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            consoleManager.sendMessage("Invalid input!");
            return false;
        }
        return n > 0 && n < 100;
    }

}
