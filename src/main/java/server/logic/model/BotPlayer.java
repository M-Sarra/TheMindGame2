package server.logic.model;

import server.logic.GameStatus;
import server.logic.TheMindGame;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BotPlayer extends Player {
    private final String name;
    private String token;
    private final TheMindGame game;
    private Timer timer;
    private List<Integer> hand;
    private LocalDateTime time;
    private final int DelayForeachCard = 500;
    private final int InitialDelay = 5000;

    public BotPlayer(String name, TheMindGame game) {
        //name is not required
        this.time = LocalDateTime.now();
        this.name = name;
        this.game = game;
        this.hand = new ArrayList<>();
        //this.token = game.Join(name,this);
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Play();
            }
        },0,100);
    }

    public void Play() {
        int count = this.hand.size();
        if (count <= 0) return ;
        Integer minCard = Integer.MAX_VALUE;
        for (int card : hand)
            if (card < minCard)
                minCard = card;
        Integer lastCard = this.game.GetLastPlayedCard();
        boolean playing = false;
        if (minCard < lastCard + 1)
            playing = true;
        else if(this.game.GetCountOfUnplayedCards() == count)
            playing = true;
        else {
            LocalDateTime playTime = time.plusNanos((this.InitialDelay +
                    (long) minCard * this.DelayForeachCard) * 1000000L);
            LocalDateTime now = LocalDateTime.now();
            if (playTime.compareTo(now) < 0) {
                playing = true;
            }
        }
        if (playing) {
            this.hand.remove(minCard);
            this.game.Play(this.token, minCard);
        }
    }

    public List<Integer> getHand() {
        return hand;
    }

    @Override
    public void StatusChanged(GameStatus status) {
        this.time = LocalDateTime.now();
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
