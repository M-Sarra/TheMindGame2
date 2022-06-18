package server;

import server.logic.TheMindGame;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.List;
import java.util.Scanner;

public class ClientManager implements Runnable {
    private final Socket socket;
    private TheMindGame game;
    private boolean isHost = true;
    private String name;
    private final String AuthToken;
    private int playerNumber;
    private final PrintWriter out;
    private final Scanner in;
    private boolean decisionTime = false;
    private boolean usingNinjaCard;
    private List<Integer> hand;

    public ClientManager(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream());
        this.AuthToken = setAuthToken();
        in = new Scanner(socket.getInputStream());
    }

    public String getName() {
        return name;
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

    public void setHand(List<Integer> hand) {
        this.hand = hand;
    }

    @Override
    public void run() {
        Server.setGame(this);
        getNameAndBotNo();
        sendMessage(AuthToken);
        getStartOrder();
    }

    private void getNameAndBotNo() {
        if (!decisionTime) {
            sendMessage("false");
        }
        name = in.nextLine();
        if (isHost) {
            playerNumber = Integer.parseInt(in.nextLine());
            for (TheMindGame game : Server.games) {
                if (game.getHost().getAuthToken().equals(this.AuthToken)) {
                    game.setPlayerNumber(this.playerNumber);
                    break;
                }
            }
        }
    }

    protected boolean decideToPLay() {
        decisionTime = true;
        sendMessage("true");
        String decision = in.nextLine();
        boolean answer = Boolean.parseBoolean(decision);
        this.isHost = !answer;
        return answer;
    }

    protected void getGame(TheMindGame game) {
        this.game = game;
    }

    //TODO
    private void getStartOrder() {
        if (isHost) {
            String message = in.nextLine();
            if (message.equals("start")) {
                for (TheMindGame game : Server.games) {
                    if (game.getHost().getAuthToken().equals(this.AuthToken)) {
                        new Thread(game).start();
                        break;
                    }
                }
            }
        }
    }

    public void decideToUseNinja(boolean haveCard) {
        if (!haveCard) {
            sendMessage("false");
            setUsingNinjaCard(false);
        }
        else {
            sendMessage("true");
            try {
                boolean useNinja = Boolean.parseBoolean(in.nextLine());
                setUsingNinjaCard(useNinja);
            } catch (Exception e) {
                setUsingNinjaCard(false);
            }
        }
    }

    //TODO
    public void start() {
        String message = "";
        try {
            message = in.nextLine();
            int cardNumber = Integer.parseInt(message);
            //check if cardNumber is valid
            if (!this.hand.contains(cardNumber)) return;
            this.game.Play(this.AuthToken, cardNumber);
        } catch (Exception ignored) {}
    }

    public void gameFinished(boolean result) {
        if (result) {
            sendMessage("Game finished. you won the game.");
        }
        else {
            sendMessage("Game finished. you lost the game.");
        }
    }

    public void sendGameStatus(String status) {
        sendMessage(status);
    }

    private void sendMessage(String message) {
        out.println(message);
        out.flush();
    }

}
