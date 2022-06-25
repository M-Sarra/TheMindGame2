package server.logic.model;

import javafx.beans.Observable;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {
    //نسخه جدید ساعت 10:20
    public String Name;

    public String Token;
    public List<Integer> hand;
    public Player player;
    public boolean ProposesNinjaCard;
    public boolean ForceToPlay1;

    public PlayerInfo( String token,Player player) {
        this.ProposesNinjaCard = true;
        this.ForceToPlay1 = false;
        this.hand = new ArrayList<>();
        this.Name = player.GetName();
        this.Token = token;
        this.player = player;
    }

    public void GiveCard(int cardNumber) {
        this.hand.add(cardNumber);
        this.player.GiveCard(cardNumber);
    }
}
