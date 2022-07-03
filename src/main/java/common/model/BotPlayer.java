package common.model;

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
    private Integer lastPlayCard;
    private final int DelayForeachCard = 2000;
    private final int InitialDelay = 5000;
    private final int countOfAgreementsToAutoAgree;
    private int currentAgreementCount = 0;
    private SecureRandom random;
    private boolean forcePlayCard;
    private boolean ninjaProposed;

    public BotPlayer(String name, int countOfAgreementsToAutoAgree) {
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
                play();
            }
        }, 0, 100);
    }

    public void join(String token, IGamePanel game) {
        this.token = token;
        this.game = game;
    }

    public void play() {
        if (this.game == null)
            return;
        GameStatus status = this.game.getStatus();
        if (status == GameStatus.Playing) {
            if (isNinjaPropose()) {
                this.ninjaProposed = true;
                this.game.proposeNinja(this.token);
            } else {
                playCard();
            }
        }
    }

    private boolean isNinjaPropose() {
        if (this.ninjaProposed)
            return false;
        if (this.forcePlayCard)
            return false;
        if (this.countOfAgreementsToAutoAgree > 0) {
            if (this.currentAgreementCount >= this.countOfAgreementsToAutoAgree)
                return true;
            return false;
        }
        if (this.game.getNinjaCards() <= 0)
            return false;
        int r = this.random.nextInt(1000);
        return r < 10;
    }

    private void playCard() {
        boolean playing = this.forcePlayCard;
        this.forcePlayCard = false;
        int count = this.hand.size();
        if (count <= 0)
            return;
        Integer minCard = Integer.MAX_VALUE;
        for (int card : hand)
            if (card < minCard)
                minCard = card;
        Integer lastCard = this.game.getLastPlayedCard();
        if (minCard < lastCard + 1)
            playing = true;
        else if (this.game.getCountOfNotPlayedCards() == count)
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
            this.game.play(this.token, minCard);
        }
    }

    public List<Integer> getHand() {
        return hand;
    }

    @Override
    public void notifyStatusChange(GameStatus status) {

        this.time = LocalDateTime.now();
        this.forcePlayCard = false;
        this.currentAgreementCount = 0;
        this.ninjaProposed = false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void giveCard(Integer card) {
        this.hand.add(card);
        this.hand.sort(Integer::compareTo);
    }

    @Override
    public void giveToken(String token) {
        this.token = token;
    }

    @Override
    public void notifyPlaysCard(String player, Integer card) {
        this.ninjaProposed = false;
        this.currentAgreementCount = 0;
    }

    @Override
    public void notifyNinjaPropose(String player) {
        this.currentAgreementCount++;
    }

    @Override
    public void notifyNinjaAgreement() {
        this.forcePlayCard = true;
    }

    @Override
    public void notifyHeartMissed() {
    }

    @Override
    public void notifyJoin(String player) {
    }

    @Override
    public void notifyLevelChange(int level) {

    }
}
