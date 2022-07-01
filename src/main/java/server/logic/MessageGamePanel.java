package server.logic;

import client.IMessageListener;
import client.IMessenger;
import server.logic.model.IGamePanel;
import server.logic.model.Player;

public class MessageGamePanel extends Player implements IMessageListener {
    private TheMindGame game;
    private IMessenger messenger;
    private String token;
    private String name;
    public MessageGamePanel(TheMindGame game,String name) {
        this.game = game;
        this.name = name;
    }

    public  void SetMessenger1(IMessenger messenger)
    {
        this.messenger = messenger;
    }
    public void SetToken(String token)
    {
        this.token = token;
    }

    @Override
    public void listen(String message) {
        if(message.startsWith("GetStatus"))
        {
            GameStatus status = this.game.getStatus();
            SendStatus(status);
            return;
        }
        if(message.startsWith("GetNinjaCards"))
        {
            int count = this.game.GetNinjaCards();
            this.messenger.sendMessage("CountOfNinjaCards:"+count);
            return;
        }
        if(message.startsWith("ProposeNinja:"))
        {
            String token = message.substring((int) "ProposeNinja:".length());

            this.game.ProposeNinja(token);
            return;
        }
        if(message.startsWith("GetLastPlayedCard"))
        {
            Integer card = this.game.GetLastPlayedCard();
            this.messenger.sendMessage("LastPlayedCardIs:"+card);
            return;
        }
        if(message.startsWith("GetCountOfUnplayedCards"))
        {
            int count = this.game.GetCountOfUnplayedCards();
            this.messenger.sendMessage("CountOfUnplayedCards:"+count);
            return;
        }
        if(message.startsWith("Play:"))
        {
            String part = message.substring((int)"Play:".length());
            Integer card = Integer.parseInt(part);
            this.game.Play(this.token,card);
            return;
        }
        if(message.startsWith("ProposeNinja"))
        {
            this.game.ProposeNinja(this.token);
            return;
        }
        throw null;
    }

    private void SendStatus(GameStatus status) {
        this.messenger.sendMessage("Status:" +status.toString());
    }

    @Override
    public void StatusChanged(GameStatus status) {
        this.SendStatus(status);
    }

    @Override
    public void NotifyPlaysCard(String player, Integer card) {
        this.messenger.sendMessage("Played:"+card+ " " +player);
    }

    @Override
    public void NotifyNinjaPropose(String player) {
        this.messenger.sendMessage("ProposedNinja:"+ player);
    }

    @Override
    public void NotifyNinjaAgreement() {
        this.messenger.sendMessage("NinjaAgreement");
    }

    @Override
    public void NotifyHeartMissed() {
        this.messenger.sendMessage("HeartMissed");
    }

    @Override
    public String GetName() {
        return this.name;
    }

    @Override
    public void GiveCard(Integer card) {
        this.messenger.sendMessage("Give "+card);

    }

}
