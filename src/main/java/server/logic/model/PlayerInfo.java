package server.logic.model;

import javafx.beans.Observable;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {

    public String Name;

    public String Token;
    public List<Integer> hand;
    private GameObserver observer;
    public PlayerInfo(String name, String token,GameObserver observer) {
        this.hand = new ArrayList<>();
        this.Name = name;
        this.Token = token;
        this.observer = observer;
    }

    public void GiveCard(int cardNumber) {
        this.hand.add(cardNumber);
        this.observer.GiveCard(cardNumber);
    }

}
