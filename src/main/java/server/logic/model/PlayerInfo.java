package server.logic.model;

import server.ClientManager;
import server.logic.GameObserver;
import server.logic.GameStatus;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo extends Player {

    private String Name;
    private String Token;
    private List<Integer> hand;
    private ClientManager clientManager;

    public PlayerInfo(String name, String token) {
        this.hand = new ArrayList<>();
        this.Name = name;
        this.Token = token;
    }

    public String getName() {
        return Name;
    }

    public String getToken() {
        return Token;
    }

    public List<Integer> getHand() {
        return hand;
    }

    @Override
    public void StatusChanged(GameStatus status) {

    }

    @Override
    public void GiveCard(Integer card) {
        this.hand.add(card);
        this.hand.sort(Integer::compareTo);
    }

    @Override
    public void NotifyPlaysCard(String player, Integer card) {

    }

    @Override
    public void NotifyHeartMissed() {

    }
}
