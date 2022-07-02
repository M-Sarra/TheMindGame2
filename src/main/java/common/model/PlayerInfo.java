package common.model;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {
    private String name;
    public String Name()
    {
        return this.name;
    }
    public String Token;
    public List<Integer> hand;
    public Player player;
    public boolean ProposesNinjaCard;
    public boolean ForceToPlay1;

    public PlayerInfo( String token,Player player) {
        this.ProposesNinjaCard = true;
        this.ForceToPlay1 = false;
        this.hand = new ArrayList<>();
        this.name = player.getName();
        this.Token = token;
        this.player = player;
    }



    public void GiveCard(int cardNumber) {
        this.hand.add(cardNumber);
        this.player.giveCard(cardNumber);
    }
}
