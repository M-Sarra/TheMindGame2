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
    public boolean HasNinjaCard;
    public boolean ForceToPlay;

    public PlayerInfo(String name, String token,Player player) {
        this.ForceToPlay = false;
        this.hand = new ArrayList<>();
        this.Name = name;
        this.Token = token;
        this.player = player;
    }

    public void GiveCard(int cardNumber) {
        this.hand.add(cardNumber);
        this.player.GiveCard(cardNumber);
    }
}
