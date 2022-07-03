package test.temp;

import common.model.Player;
import server.logic.GameController;
import common.model.GameStatus;
import server.logic.TheMindGame;

public class MessageGamePanel extends Player implements IMessageListener {
    private GameController controller;
    private TheMindGame game;
    private IMessenger messenger;
    private String token;
    private String name;

    public MessageGamePanel(GameController controller) {
        this.controller = controller;
    }

    public void setMessenger(IMessenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public void listen(String message) {
        if (message.startsWith("Register:")) {
            this.name = message.substring((int) "Register:".length());
            String token = this.controller.register(this);
            //String result = controller.Join(token, gameName);
            this.token = token;
            //this.messenger.sendMessage("Token:"+token);
            return;
        }
        if (message.startsWith("JoinTo:")) {
            String gameName = message.substring((int) "JoinTo:".length());
            this.game = this.controller.getGameByName(gameName);
            String result = this.controller.join(this.token, gameName);
            this.messenger.sendMessage("JoinResult:" + result);
            return;
        }

        if (message.startsWith("GetStatus")) {
            if (this.game == null) {
                this.messenger.sendMessage("InvalidGame");
                return;
            }
            GameStatus status = this.game.getStatus();
            sendStatus(status);
            return;
        }
        if (message.startsWith("GetNinjaCards")) {
            if (this.game == null) {
                this.messenger.sendMessage("InvalidGame");
                return;
            }
            int count = this.game.getNinjaCards();
            this.messenger.sendMessage("CountOfNinjaCards:" + count);
            return;
        }
        if (message.startsWith("ProposeNinja:")) {
            if (this.game == null) {
                this.messenger.sendMessage("InvalidGame");
                return;
            }
            String token = message.substring((int) "ProposeNinja:".length());

            this.game.proposeNinja(token);
            return;
        }
        if (message.startsWith("GetLastPlayedCard")) {
            if (this.game == null) {
                this.messenger.sendMessage("InvalidGame");
                return;
            }
            Integer card = this.game.getLastPlayedCard();
            this.messenger.sendMessage("LastPlayedCardIs:" + card);
            return;
        }
        if (message.startsWith("GetCountOfNotPlayedCards")) {
            if (this.game == null) {
                this.messenger.sendMessage("InvalidGame");
                return;
            }
            int count = this.game.getCountOfNotPlayedCards();
            this.messenger.sendMessage("CountOfNotPlayedCards:" + count);
            return;
        }
        if (message.startsWith("Play:")) {
            if (this.game == null) {
                this.messenger.sendMessage("InvalidGame");
                return;
            }
            String part = message.substring((int) "Play:".length());
            Integer card = Integer.parseInt(part);
            this.game.play(this.token, card);
            return;
        }
        if (message.startsWith("ProposeNinja")) {
            if (this.game == null) {
                this.messenger.sendMessage("InvalidGame");
                return;
            }
            this.game.proposeNinja(this.token);
            return;
        }
        throw null;
    }

    private void sendStatus(GameStatus status) {
        this.messenger.sendMessage("Status:" + status.toString());
    }

    @Override
    public void notifyStatusChange(GameStatus status) {
        this.sendStatus(status);
    }

    @Override
    public void notifyPlaysCard(String player, Integer card) {
        this.messenger.sendMessage("Played:" + card + " " + player);
    }

    @Override
    public void notifyNinjaPropose(String player) {
        this.messenger.sendMessage("ProposedNinja:" + player);
    }

    @Override
    public void notifyNinjaAgreement() {
        this.messenger.sendMessage("NinjaAgreement");
    }

    @Override
    public void notifyHeartMissed() {
        this.messenger.sendMessage("HeartMissed");
    }

    @Override
    public void notifyJoin(String player) {
        this.messenger.sendMessage("Joined:" + player);
    }

    @Override
    public void notifyLevelChange(int level) {

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void giveCard(Integer card) {
        this.messenger.sendMessage("Give " + card);

    }

    @Override
    public void giveToken(String token) {
        this.token = token;
        this.messenger.sendMessage("Token:" + token);
    }
}
