package server.logic;

import server.logic.model.GameObserver;
import server.logic.model.Player;
import server.logic.model.PlayerInfo;

import java.security.*;
import java.util.*;

public class TheMindGame {
    private GameStatus status;
    private int level;
    private SecureRandom random;
    private List<PlayerInfo> players;
    private List<GameObserver> observers;
    private List<Integer> usedCards;
    private  int heartCards;
    private Integer lastPlayedCard;

    public TheMindGame()
    {
        this.heartCards = 0;
        this.level = 0;
        this.status = GameStatus.NotStarted;
        this.usedCards = new ArrayList<>();
        this.random = new SecureRandom();// SecureRandom.getInstanceStrong();
        this.players = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public String Join(String name, Player observer)
    {
        if(this.status != GameStatus.NotStarted)
            return "Invalid connecting time";
        PlayerInfo player = GetPlayerByName(name);
        if(player != null)
            return "duplicative name";
        String token = GetUnusedToken();
        player = new PlayerInfo(name,token,observer);
        this.players.add(player);
        this.observers.add(observer);
        return token;
    }

    public  void  AddObserver(GameObserver observer)
    {
        this.observers.add(observer);
    }

    private String GetUnusedToken() {
        PlayerInfo player;
        String token;
        do {
            token= String.valueOf(this.random.nextLong());
            player = GetPlayerByToken(token);
        }while (player != null);
        return token;
    }

    private PlayerInfo GetPlayerByName(String name) {
        for (PlayerInfo player: this.players) {
            if(player.Name.equals(name))
                return player;
        }
        return null;
    }
    private PlayerInfo GetPlayerByToken(String token) {
        for (PlayerInfo player: this.players) {
            if(player.Token.equals(token))
                return player;
        }
        return null;
    }

    public String Start()
    {
        if(this.status != GameStatus.NotStarted)
            return "Game is "+this.status;
        this.ChangeStatus(GameStatus.Starting);
        this.DealHeartCards();
        ChangeLevel(1);
        return "Success";
    }

    private void ChangeLevel(int level) {
        if(level > 25) {
            this.ChangeStatus(GameStatus.GameOver);
            return;
        }
        this.level = level;
        this.lastPlayedCard = 0;
        this.usedCards.clear();
        this.Deal();
        this.ChangeStatus(GameStatus.LevelStarted);
    }

    private void DealHeartCards() {
        int count = (int)this.players.stream().count();
        this.heartCards = count;
    }

    private void Deal() {
        for (PlayerInfo player:this.players) {
            for (int i = 0 ; i < this.level;i++) {
                int cardNumber = this.GetUnusedRandomCard();
                this.usedCards.add(cardNumber);
                player.GiveCard(cardNumber);
            }
        }
    }

    private int GetUnusedRandomCard() {

        int card ;
        do {
            card = this.random.nextInt(100)+1;
        }while (this.usedCards.contains(card));
        return card;
    }

    private void ChangeStatus(GameStatus newStatus) {
        this.status = newStatus;
        this.NotifyChangeStatus(newStatus);
    }

    private void NotifyChangeStatus(GameStatus status) {
        Thread inform = new Thread(()->{
            for (GameObserver observer:this.observers
            ) {
                /*Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                    }
                };*/

                Thread observerInform = new Thread(()->          observer.StatusChanged(status));
                observerInform.start();;
            }
        });
        inform.start();
    }
    private void NotifyPlayingCard(String player,int card) {
        Thread inform = new Thread(()->{
            for (GameObserver observer:this.observers
            ) {
                Thread observerInform = new Thread(()-> observer.NotifyPlaysCard(player,card));
                observerInform.start();;
            }
        });
        inform.start();
    }
    private void NotifyHeartMissed() {
        Thread inform = new Thread(()->{
            for (GameObserver observer:this.observers
            ) {
                Thread observerInform = new Thread(()-> observer.NotifyHeartMissed());
                observerInform.start();;
            }
        });
        inform.start();
    }

    public String Play(String token, Integer card) {
        if(this.status !=  GameStatus.LevelStarted)
            return "Invalid action";
        PlayerInfo player = this.GetPlayerByToken(token);
        if(player == null)
            return "Invalid player";
        if(!player.hand.contains(card))
            return "Invalid Card";
        NotifyPlayingCard(player.Name,card);
        this.usedCards.remove(card);
        if(card < this.lastPlayedCard)
            this.MissHeart();
        this.lastPlayedCard = card;
        if(this.status == GameStatus.GameOver)
            return "Game Over";
        if(this.usedCards.stream().count() <= 0)
            this.ChangeLevel(this.level+1);
        return "Success";
    }

    private void MissHeart() {
        this.heartCards --;
        this.NotifyHeartMissed();
        if(this.heartCards == 0)
            this.ChangeStatus(GameStatus.GameOver);
    }

    public Integer GetLastPlayedCard() {
        return this.lastPlayedCard;

    }

    public int GetCountOfUnplayedCards() {
        return (int)this.usedCards.stream().count();
    }

    public GameStatus getStatus() {
        return this.status;
    }
}
