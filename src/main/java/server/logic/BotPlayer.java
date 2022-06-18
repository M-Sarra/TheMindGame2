package server.logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BotPlayer extends Player {
    private String name;
    private String token;
    private TheMindGame game;
    private Timer timer;
    private List<Integer> hand;
    private LocalDateTime time;
    private int DelayForeachCard = 500;
    private int InitialDelay = 5000;

    public BotPlayer(String name, TheMindGame game) {
        this.time = LocalDateTime.now();
        this.name = name;
        this.game = game;
        this.hand = new ArrayList<>();
        this.token = game.Join(name,this);
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Play();
            }
        },0,100);
    }

    public  void Play()
    {
       int count =(int)this.hand.stream().count();
        if(count <= 0)
            return ;
        Integer minCard = Integer.MAX_VALUE;
        for( Integer card : hand)
            if(card < minCard)
                minCard = card;
        Integer lastCard = this.game.GetLastPlayedCard();
        boolean playing = false;
        if(minCard < lastCard+1)
            playing = true;
        if(this.game.GetCountOfUnplayedCards() == count)
            playing = true;
        else {
            LocalDateTime playTime = time.plusNanos((this.InitialDelay + minCard * this.DelayForeachCard) * 1000000l);
            LocalDateTime now = LocalDateTime.now();
            if (playTime.compareTo(now) < 0) {
                playing = true;
            }
        }
        if(playing)
        {
            this.hand.remove(minCard);
            this.game.Play(this.token,minCard);
        }
    }


    @Override
    public void StatusChanged(GameStatus status) {
        this.time = LocalDateTime.now();
        switch (status)
        {

        }
    }

    @Override
    public void GiveCard(Integer card) {
        this.hand.add(card);
        this.hand.sort((x,y)-> x.compareTo(y));
    }

    @Override
    public void NotifyPlaysCard(String player, Integer card) {

    }

    @Override
    public void NotifyHeartMissed() {

    }

}
