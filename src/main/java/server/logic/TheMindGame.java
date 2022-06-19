package server.logic;

import server.logic.model.GameObserver;
import server.logic.model.Player;
import server.logic.model.PlayerInfo;

import java.security.*;
import java.util.*;

public class TheMindGame {
    public String Name;
    private GameController controller;
    private GameStatus status;
    private int level;
    private List<PlayerInfo> players;
    private List<GameObserver> observers;
    private List<Integer> usedCards;
    private  int heartCards;
    private Integer lastPlayedCard;
    private SecureRandom random;

    public TheMindGame(String name,GameController controller)
    {
        //برای تست کردن نوشتیم.
        this.Name = name;
        this.controller = controller;
        this.heartCards = 0;
        this.level = 0;
        this.random = new SecureRandom();
        this.status = GameStatus.NotStarted;
        this.usedCards = new ArrayList<>();
        this.players = new ArrayList<>();
        this.observers = new ArrayList<>();
    }


    public  void  AddObserver(GameObserver observer)
    {
        this.observers.add(observer);
    }

    public String Start()
    {
        if(this.status != GameStatus.NotStarted)
            return "Game is "+this.status;
        this.ChangeStatus(GameStatus.Starting);
        this.DealHeartCards();
        this.DealNinjaCards();
        ChangeLevel(1);
        return "Success";
    }

    private void ChangeLevel(int level) {
        if(level > 12) {
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

    private void DealNinjaCards() {
        for (PlayerInfo player: this.players) {
            player.HasNinjaCard = true;
        }
    }
    private void ForceToPlay() {
        for (PlayerInfo player:this.players) {
             if(player.hand.stream().count() > 0)
                 player.ForceToPlay = true;
        }
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

    private void NotifyPlayingNinjaCard(String player) {
        Thread inform = new Thread(()->{
            for (GameObserver observer:this.observers) {
                Thread observerInform = new Thread(()-> observer.NotifyPlaysNinjaCard(player));
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

    public void AddPlayer(PlayerInfo player) {
        this.players.add(player);
        this.observers.add(player.player);
    }

    public  String PlayNinja(String token)
    {
        synchronized (this) {
            if (this.status != GameStatus.LevelStarted && this.status != GameStatus.NinjaPlayed)
                return "Invalid action";
            PlayerInfo player = this.controller.GetPlayerByToken(token);
            if (player == null)
                return "Invalid player";
            if (!player.HasNinjaCard)
                return "No Ninja card";
            NotifyPlayingNinjaCard(player.Name);
            this.ChangeStatus(GameStatus.NinjaPlayed);
            return "Success";
        }
    }

    public String Play(String token, Integer card) {
        synchronized (this) {
            if (this.status != GameStatus.LevelStarted && this.status != GameStatus.NinjaPlayed)
                return "Invalid action";
            PlayerInfo player = this.controller.GetPlayerByToken(token);
            if (player == null)
                return "Invalid player";
            if (!player.hand.contains(card))
                return "Invalid Card";
            NotifyPlayingCard(player.Name, card);
            if (!player.ForceToPlay) {
                this.usedCards.remove(card);
                if (card < this.lastPlayedCard)
                    this.MissHeart();
                this.lastPlayedCard = card;
                if (this.status == GameStatus.GameOver)
                    return "Game Over";
                if (this.usedCards.stream().count() <= 0)
                    this.ChangeLevel(this.level + 1);
            }
            player.ForceToPlay = false;
            return "Success";
        }
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
