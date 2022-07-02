package server.logic;

import common.model.GameStatus;
import server.log.ILogger;
import common.model.GameObserver;
import common.model.IGamePanel;
import common.model.PlayerInfo;

import java.security.*;
import java.util.*;

public class TheMindGame implements IGamePanel {
    private String name;
    private GameController controller;
    private GameStatus status;
    private int level;
    private List<PlayerInfo> players;
    private List<GameObserver> observers;
    private List<Integer> usedCards;
    private int heartCards;
    private int ninjaCards;
    private Integer lastPlayedCard;
    private Integer wrongCard;
    private SecureRandom random;
    private PlayerInfo host;
    private int capacity;
    private ILogger logger;

    public TheMindGame(String name, GameController controller, PlayerInfo host, int capacity, ILogger logger) {
        this.logger = logger;
        this.host = host;
        this.capacity = capacity;
        this.name = name;
        this.controller = controller;
        this.heartCards = 0;
        this.level = 0;
        this.random = new SecureRandom();
        this.status = GameStatus.NotStarted;
        this.usedCards = new ArrayList<>();
        this.players = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public String getHostName() {
        return this.host.Name();
    }

    public void addObserver(GameObserver observer) {
        synchronized (this.observers) {
            this.observers.add(observer);
        }
    }

    public String start() {
        if (this.status != GameStatus.NotStarted)
            return "Game is " + this.status;
        this.changeStatus(GameStatus.Starting);
        this.dealHeartCards();
        this.ninjaCards = 2;
        this.resetNinjas();
        changeLevel(1);
        return "Success";
    }

    private void changeLevel(int level) {
        if (level > 12) {
            this.changeStatus(GameStatus.Win);
            this.changeStatus(GameStatus.GameOver);
            return;
        }
        this.changeStatus(GameStatus.Dealing);
        this.level = level;
        this.lastPlayedCard = 0;
        this.wrongCard = 0;
        this.usedCards.clear();
        this.deal();
        this.changeStatus(GameStatus.Playing);
    }

    private void dealHeartCards() {
        int count = (int) this.players.stream().count();
        this.heartCards = count;
    }

    private void resetNinjas() {
        for (PlayerInfo player : this.players) {
            player.ProposesNinjaCard = false;
        }
    }

    private void forceToPlay() {
        for (PlayerInfo player : this.players) {
            if (player.hand.stream().count() > 0)
                player.ForceToPlay1 = true;
        }
    }

    private void deal() {
        try {
            Thread.sleep(3000);
        } catch (Exception ex) {
        }
        for (PlayerInfo player : this.players) {
            for (int i = 0; i < this.level; i++) {
                int cardNumber = this.getUnusedRandomCard();
                this.usedCards.add(cardNumber);
                player.GiveCard(cardNumber);
            }
        }
    }

    private int getUnusedRandomCard() {
        int card;
        do {
            card = this.random.nextInt(100) + 1;
        } while (this.usedCards.contains(card));
        return card;
    }

    private void changeStatus(GameStatus newStatus) {
        this.status = newStatus;
        this.notifyChangeStatus(newStatus);
    }

    private void notifyChangeStatus(GameStatus status) {
        synchronized (this.observers) {
            for (GameObserver observer : this.observers
            ) {
                Thread observerInform = new Thread(() ->
                {
                    try {
                        observer.statusChanged(status);
                    } catch (Exception ex) {
                        this.logger.log(ex.toString());
                    }
                });
                observerInform.start();
            }
        }
    }

    private void notifyPlayingCard(String player, int card) {
        synchronized (this.observers) {
            for (GameObserver observer : this.observers
            ) {
                Thread observerInform = new Thread(() ->{
                    try {
                        observer.notifyPlaysCard(player, card);
                    }
                    catch (Exception ex)
                    {
                        this.logger.log(ex.getMessage());
                    }
                });
                observerInform.start();
            }
        }
    }

    private void notifyJoin(String player) {
        synchronized (this.observers) {
            for (GameObserver observer : this.observers
            ) {
                Thread observerInform = new Thread(() ->
                {
                    try {
                        observer.notifyJoin(player);
                    } catch (Exception ex) {
                        this.logger.log(ex.getMessage());
                    }
                    ;
                });
                observerInform.start();
            }
        }
    }

    private void notifyNinjaPropose(String player) {
        synchronized (this.observers) {
            for (GameObserver observer : this.observers) {
                Thread observerInform = new Thread(() -> {
                    try {
                        observer.notifyNinjaPropose(player);
                    } catch (Exception ex) {
                        this.logger.log(ex.getMessage());
                    }
                });
                observerInform.start();
            }
        }
    }

    private void notifyNinjaAgreement() {
        synchronized (this.observers) {
            for (GameObserver observer : this.observers) {
                Thread observerInform = new Thread(() -> {
                    try {
                        observer.notifyNinjaAgreement();
                    }
                    catch (Exception ex)
                    {
                        this.logger.log(ex.getMessage());
                    }
                    });
                observerInform.start();
            }
        }
    }

    private void notifyHeartMissed() {
        synchronized (this.observers) {
            for (GameObserver observer : this.observers
            ) {
                Thread observerInform = new Thread(() ->
                {
                    try {
                        observer.notifyHeartMissed();
                    }catch (Exception ex)
                    {
                        this.logger.log(ex.getMessage());
                    }
                });
                observerInform.start();
                ;
            }
        }
    }

    public String join(PlayerInfo player) {
        if (this.players.stream().count() >= this.capacity)
            return "Game is full";
        for (PlayerInfo info : this.players) {
            if (info.Token.compareTo(player.Token) == 0)
                return "Duplicate Player";
        }
        this.players.add(player);
        synchronized (this.observers) {
            this.observers.add(player.player);
        }
        this.notifyJoin(player.Name());
        return "Success";
    }

    public String proposeNinja(String token) {
        synchronized (this) {
            if (this.status != GameStatus.Playing)
                return "Invalid action";
            PlayerInfo player = this.controller.getPlayerByToken(token);
            if (player == null)
                return "Invalid player";
            if (this.ninjaCards <= 0)
                return "No Ninja card";
            player.ProposesNinjaCard = true;
            notifyNinjaPropose(player.Name());
            if (isNinjaAgreement()) {
                notifyNinjaAgreement();
                this.ninjaCards--;
                this.forceToPlay();
            }
            //this.ChangeStatus(GameStatus.NinjaPlayed);
            return "Success";
        }
    }

    private boolean isNinjaAgreement() {
        for (PlayerInfo player : this.players) {
            if (!player.ProposesNinjaCard)
                return false;

        }
        return true;
    }

    @Override
    public String play(String token, Integer card) {
        synchronized (this) {
            if (this.status != GameStatus.Playing)
                return "Invalid action";
            PlayerInfo player = this.controller.getPlayerByToken(token);
            if (player == null)
                return "Invalid player";
            if (!player.hand.contains(card))
                return "Invalid Card";
            notifyPlayingCard(player.Name(), card);
            this.usedCards.remove(card);
            if (!player.ForceToPlay1) {
                if (card < this.lastPlayedCard && card > this.wrongCard) {
                    this.missHeart();
                    this.wrongCard = this.lastPlayedCard;
                }
                this.lastPlayedCard = card;
            }
            if (this.status == GameStatus.GameOver)
                return "Game Over";
            if (this.usedCards.stream().count() <= 0)
                this.changeLevel(this.level + 1);
            player.ForceToPlay1 = false;
            this.resetNinjas();
            return "Success";
        }
    }

    private void missHeart() {
        this.heartCards--;
        this.notifyHeartMissed();
        if (this.heartCards == 0) {
            this.changeStatus(GameStatus.Lost);
            try {
                Thread.sleep(1000);
            }
            catch (Exception ex)
            {
                this.logger.log(ex.getMessage());
            }
            this.changeStatus(GameStatus.GameOver);
        }
    }

    public Integer getLastPlayedCard() {
        return this.lastPlayedCard;

    }

    public int getCountOfNotPlayedCards() {
        return (int) this.usedCards.stream().count();
    }

    public GameStatus getStatus() {
        return this.status;
    }

    public List<String> getPlayersName() {
        List<String> names = new ArrayList<>();
        for (PlayerInfo player : this.players) {
            if (!player.Name().contains("Bot")) names.add(player.Name());
        }
        return names;
    }

    public int getHeartNumber() {
        return this.heartCards;
    }

    public int getNinjaCards() {
        return this.ninjaCards;
    }

    public int getCountOfPlayers() {
        return (int) this.players.stream().count();
    }

    public boolean isJoinable() {
        if (status != GameStatus.NotStarted)
            return false;
        if (this.players.stream().count() >= this.capacity)
            return false;
        return true;
    }

    public int getLevel() {
        return this.level;
    }

    public void stop() {
        this.changeStatus(GameStatus.Stoped);
    }

    public String getName() {
        return this.name;
    }
}
