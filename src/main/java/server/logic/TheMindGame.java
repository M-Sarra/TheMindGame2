package server.logic;

import server.logic.model.GameObserver;
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
    private int ninjaCards;
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
        this.ninjaCards = 2;
        this.ResetNinjas();
        ChangeLevel(1);
        return "Success";
    }

    private void ChangeLevel(int level) {
        if(level > 12) {
            this.ChangeStatus(GameStatus.Win);
            this.ChangeStatus(GameStatus.GameOver);
            return;
        }
        this.ChangeStatus(GameStatus.Dealing);
        this.level = level;
        this.lastPlayedCard = 0;
        this.usedCards.clear();
        this.Deal();
        this.ChangeStatus(GameStatus.Playing);
    }

    private void DealHeartCards() {
        int count = (int)this.players.stream().count();
        this.heartCards = count;
    }

    private void ResetNinjas() {
        for (PlayerInfo player: this.players) {
            player.ProposesNinjaCard = false;
        }
    }
    private void ForceToPlay() {
        for (PlayerInfo player:this.players) {
            if(player.hand.stream().count() > 0)
                player.ForceToPlay1 = true;
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

    private void NotifyNinjaPropose(String player) {
        Thread inform = new Thread(()->{
            for (GameObserver observer:this.observers) {
                Thread observerInform = new Thread(()-> observer.NotifyNinjaPropose(player));
                observerInform.start();;
            }
        });
        inform.start();
    }
    private void NotifyNinjaAgreement() {
        Thread inform = new Thread(()->{
            for (GameObserver observer:this.observers) {
                Thread observerInform = new Thread(()-> observer.NotifyNinjaAgreement());
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

    public  String ProposeNinja(String token)
    {
        synchronized (this) {
            if (this.status != GameStatus.Playing)
                return "Invalid action";
            PlayerInfo player = this.controller.GetPlayerByToken(token);
            if (player == null)
                return "Invalid player";
            if (this.ninjaCards <= 0)
                return "No Ninja card";
            player.ProposesNinjaCard = true;
            NotifyNinjaPropose(player.Name);
            if(IsNinjaAgreement())
            {
                NotifyNinjaAgreement();
                this.ninjaCards--;
                this.ForceToPlay();
            }
            //this.ChangeStatus(GameStatus.NinjaPlayed);
            return "Success";
        }
    }

    private boolean IsNinjaAgreement() {
        for (PlayerInfo player:this.players             ) {
            if(!player.ProposesNinjaCard)
                return false;

        }
        return true;
    }

    public String Play(String token, Integer card) {
        synchronized (this) {
            if (this.status != GameStatus.Playing)
                return "Invalid action";
            PlayerInfo player = this.controller.GetPlayerByToken(token);
            if (player == null)
                return "Invalid player";
            if (!player.hand.contains(card))
                return "Invalid Card";
            NotifyPlayingCard(player.Name, card);
            this.usedCards.remove(card);
            if (!player.ForceToPlay1) {
                if (card < this.lastPlayedCard)
                    this.MissHeart();
                this.lastPlayedCard = card;
            }
            if (this.status == GameStatus.GameOver)
                return "Game Over";
            if (this.usedCards.stream().count() <= 0)
                this.ChangeLevel(this.level + 1);
            player.ForceToPlay1 = false;
            this.ResetNinjas();
            return "Success";
        }
    }

    private void MissHeart() {
        this.heartCards --;
        this.NotifyHeartMissed();
        if(this.heartCards == 0) {
            this.ChangeStatus(GameStatus.Lost);
            this.ChangeStatus(GameStatus.GameOver);
        }
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

    //TODO : to send message to client
    public List<String> getPlayersName() {
        List<String> names = new ArrayList<>();
        for (PlayerInfo player : this.players) {
            if (!player.Name.contains("Bot")) names.add(player.Name);
        }
        return names;
    }

    //TODO : send heart number
    public int getHeartNumber() {
        return this.heartCards;
    }

    public  int GetNinjaCards()
    {
        return this.ninjaCards;
    }
}
