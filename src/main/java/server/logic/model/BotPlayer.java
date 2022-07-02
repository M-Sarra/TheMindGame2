package server.logic.model;

import server.logic.GameStatus;
import server.logic.TheMindGame;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BotPlayer extends Player {
    private final String name;
    private String token;
    private IGamePanel game;
    private Timer timer;
    private List<Integer> hand;
    private LocalDateTime time;
    private final int DelayForeachCard = 2000;
    private final int InitialDelay = 5000;
    private final int countOfAgreementsToAutoAgree;
    private int currentAgreementCount = 0;
    private SecureRandom random;
    private boolean forcePlayCard;
    private boolean ninjaProposed;

    public BotPlayer(String name,int countOfAgreementsToAutoAgree) {
        //name is not required
        this.countOfAgreementsToAutoAgree = countOfAgreementsToAutoAgree;
        this.currentAgreementCount = 0;
        this.ninjaProposed = false;
        this.random = new SecureRandom();
        this.forcePlayCard = false;
        this.time = LocalDateTime.now();
        this.name = name;
        this.hand = new ArrayList<>();
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Play();
            }
        },0,100);
    }
    public void Join(String token, IGamePanel game)
    {
        this.token = token;
        this.game = game;
    }

    public  void Play() {
        if(this.game == null)
            return;
        GameStatus status = this.game.getStatus();
        if(status == GameStatus.Playing) {
            if (IsNinjaPropose()) {
                this.ninjaProposed = true;
                this.game.ProposeNinja(this.token);
            }
            else {
                PlayCard();
            }
        }
    }

    private boolean IsNinjaPropose() {
        if(this.ninjaProposed)
            return false;
        if(this.forcePlayCard)
            return false;
        if(this.countOfAgreementsToAutoAgree > 0) {
            if (this.currentAgreementCount >= this.countOfAgreementsToAutoAgree)
                return true;
            return false;
        }
        if(this.game.GetNinjaCards()<= 0)
            return false;
        int r = this.random.nextInt(1000);
        return r < 10;
    }

    private void PlayCard() {
        boolean playing = this.forcePlayCard;
        this.forcePlayCard = false;
        int count = this.hand.size();
        if (count <= 0)
            return;
        Integer minCard = Integer.MAX_VALUE;
        for (int card : hand)
            if (card < minCard)
                minCard = card;
        Integer lastCard = this.game.GetLastPlayedCard();
        if (minCard < lastCard + 1)
            playing = true;
        else if (this.game.GetCountOfUnplayedCards() == count)
            playing = true;
        else {
            LocalDateTime playTime = time.plusNanos((this.InitialDelay + (long) minCard * this.DelayForeachCard) * 1000000L);
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
        this.forcePlayCard = false;
        this.currentAgreementCount = 0;
        this.ninjaProposed = false;
    }

    @Override
    public String GetName() {
        return this.name;
    }

    @Override
    public void GiveCard(Integer card) {
        this.hand.add(card);
        this.hand.sort(Integer::compareTo);
    }

    @Override
    public void NotifyPlaysCard(String player, Integer card)
    {
        this.ninjaProposed = false;
        this.currentAgreementCount = 0;
    }

    @Override

    public void NotifyNinjaPropose(String player) {
        this.currentAgreementCount++;
    }
    @Override
    public void NotifyNinjaAgreement() {
        this.forcePlayCard = true;
    }

    @Override
    public void NotifyHeartMissed() {

    }

}
