package server.logic;

import server.ClientManager;
import server.logic.model.BotPlayer;
import server.logic.model.PlayerInfo;

import java.security.SecureRandom;
import java.util.*;

public class TheMindGame implements Runnable {
    private GameStatus status;
    private int level;
    private SecureRandom random;
    private final List<PlayerInfo> players;
    private final List<BotPlayer> botPlayers;
    private List<GameObserver> observers;
    private List<Integer> usedCards;
    private int heartCards;
    private int ninjaCards;
    private Integer lastPlayedCard;
    private List<ClientManager> clientManagers;
    private ClientManager host;
    private int playerNumber;

    public TheMindGame() {
        this.heartCards = 0;
        this.level = 0;
        this.ninjaCards = 2;
        this.status = GameStatus.NotStarted;
        this.usedCards = new ArrayList<>();
        this.random = new SecureRandom();// SecureRandom.getInstanceStrong();
        this.players = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.clientManagers = new ArrayList<>();
        this.botPlayers = new ArrayList<>();
    }

    public int getClientManagersNumber() {
        return this.clientManagers.size();
    }

    public List<ClientManager> getClientManagers() {
        return clientManagers;
    }

    public int getPlayerNumber() {
        return this.playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public ClientManager getHost() {
        return host;
    }

    public void setHost(ClientManager host) {
        this.host = host;
    }

    public void addClientManager(ClientManager clientManager) {
        this.clientManagers.add(clientManager);
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public void setHeartCards(int heartCards) {
        this.heartCards = heartCards;
    }

    public void increaseHeartCards() {
        this.heartCards++;
    }

    public void join(String name, String token) {
        if(this.status != GameStatus.NotStarted) return;
        PlayerInfo player = GetPlayerByName(name);
        if(player != null) return;
        //String token = GetUnusedToken();
        player = new PlayerInfo(name, token);
        this.players.add(player);
        //this.observers.add(observer);
    }

    public void AddObserver(GameObserver observer) {
        this.observers.add(observer);
    }

    public void addPlayer(PlayerInfo player) {
        this.players.add(player);
    }

    private String GetUnusedToken() {
        PlayerInfo player;
        String token;
        do {
            token= String.valueOf(this.random.nextLong());
            player = GetPlayerByToken(token);
        } while (player != null);
        return token;
    }

    private PlayerInfo GetPlayerByName(String name) {
        for (PlayerInfo player: this.players) {
            if(player.getName().equals(name))
                return player;
        }
        return null;
    }

    private PlayerInfo GetPlayerByToken(String token) {
        for (PlayerInfo player: this.players) {
            if (player.getToken().equals(token))
                return player;
        }
        return null;
    }

    public String start() {
        if (this.status != GameStatus.NotStarted)
            return "Game is " + this.status;
        setPlayers();
        this.ChangeStatus(GameStatus.Starting);
        this.DealHeartCards();
        playRound();
        return "Success";
    }

    private void ChangeLevel() {
        this.level++;
        if (level > 12) {
            this.ChangeStatus(GameStatus.win);
            return;
        }
        this.lastPlayedCard = 0;
        this.usedCards.clear();
        this.Deal();
        this.ChangeStatus(GameStatus.LevelStarted);
        if (this.level % 3 == 0 && this.level < 12) {
            increaseHeartCards();
        }
        if (this.level % 3 == 2 && this.level < 11) {
            this.ninjaCards++;
        }
    }

    private void DealHeartCards() {
        this.heartCards = this.playerNumber;
    }

    private void Deal() {
        for (PlayerInfo player : this.players) {
            for (int i = 0 ; i < this.level ; i++) {
                int cardNumber = this.GetUnusedRandomCard();
                this.usedCards.add(cardNumber);
                player.GiveCard(cardNumber);
            }
        }
        for (BotPlayer bot : this.botPlayers) {
            for (int i = 0 ; i < this.level ; i++) {
                int cardNumber = this.GetUnusedRandomCard();
                this.usedCards.add(cardNumber);
                bot.GiveCard(cardNumber);
            }
        }
    }

    private int GetUnusedRandomCard() {
        int card ;
        do {
            card = this.random.nextInt(100) + 1;
        } while (this.usedCards.contains(card));
        return card;
    }

    private void ChangeStatus(GameStatus newStatus) {
        this.status = newStatus;
        this.NotifyChangeStatus(newStatus);
    }

    private void NotifyChangeStatus(GameStatus status) {
        Thread inform = new Thread(()->{
            for (GameObserver observer : this.observers
            ) {
                /*Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                    }
                };*/

                Thread observerInform = new Thread(()-> observer.StatusChanged(status));
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
            for (GameObserver observer : this.observers) {
                Thread observerInform = new Thread(observer::NotifyHeartMissed);
                observerInform.start();
            }
        });
        inform.start();
    }

    public String Play(String token, Integer card) {
        if (this.status != GameStatus.LevelStarted)
            return "Invalid action";
        PlayerInfo player = this.GetPlayerByToken(token);
        if (player == null)
            return "Invalid player";
        if (!player.getHand().contains(card))
            return "Invalid Card";
        NotifyPlayingCard(player.getName(),card);
        this.usedCards.remove(card);
        if (card < this.lastPlayedCard)
            this.MissHeart();
        this.lastPlayedCard = card;
        if (this.status == GameStatus.GameOver)
            return "Game Over";
        if ((long) this.usedCards.size() <= 0)
            this.ChangeLevel();
        return "Success";
    }

    private void MissHeart() {
        this.heartCards --;
        this.NotifyHeartMissed();
        if (this.heartCards == 0) this.ChangeStatus(GameStatus.GameOver);
    }

    public Integer GetLastPlayedCard() {
        return this.lastPlayedCard;
    }

    public int GetCountOfUnplayedCards() {
        return this.usedCards.size();
    }

    private void playRound() {
        for (int i = 1; i <= 12; i++) {
            if (this.heartCards == 0) return;
            ChangeLevel();
            for (int j = 1; j < playerNumber * i; j++) {
                if (!useNinjaCard()) {
                    runPlayersStartMethod();
                    sendGameStatus();
                }
            }
        }
    }

    private boolean useNinjaCard() {
        if (ninjaCards == 0) return false;
        List<Thread> threads = new ArrayList<>();
        if (this.usedCards.size() > 1) {
            int i = 0;
            for (ClientManager clientManager : this.clientManagers) {
                for (PlayerInfo player : this.players) {
                    if (player.getToken().equals(clientManager.getAuthToken())) {
                        Thread thread;
                        if (player.getHand().size() > 0) {
                            i++;
                            thread = new Thread(() -> clientManager.decideToUseNinja(true));
                        }
                        else {
                            thread = new Thread(() -> clientManager.decideToUseNinja(false));
                        }
                        threads.add(thread);
                        thread.start();
                        break;
                    }
                }
            }
            if (i == 0) return false;
        }
        else return false;
        final boolean[] useNinjaCard1 = new boolean[1];
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean useNinjaCard = true;
                for (Thread thread : threads) {
                    thread.interrupt();
                }
                for (ClientManager clientManager : clientManagers) {
                    if (!clientManager.isUsingNinjaCard())  {
                        useNinjaCard = false;
                        break;
                    }
                }
                useNinjaCard1[0] = useNinjaCard;
            }
        }, 7000);
        if (useNinjaCard1[0]) {
            List<Integer> thisRoundCards = new LinkedList<>();
            for (PlayerInfo player : players) {
                try {
                    int card = Collections.min(player.getHand());
                    thisRoundCards.add(card);
                    player.getHand().remove(card);
                } catch (Exception ignored) {}
            }
            for (BotPlayer bot : botPlayers) {
                try {
                    int card = Collections.min(bot.getHand());
                    thisRoundCards.add(card);
                    bot.getHand().remove(card);
                } catch (Exception ignored) {}
            }
            this.usedCards.removeAll(thisRoundCards);
            this.lastPlayedCard = Collections.max(thisRoundCards);
            return true;
        }
        return false;
    }

    private void runPlayersStartMethod() {
        List<Thread> threads = new ArrayList<>();
        for (ClientManager clientManager : this.clientManagers) {
            Thread thread = new Thread(clientManager::start);
            threads.add(thread);
            thread.start();
        }
        for (BotPlayer bot : this.botPlayers) {
            Thread thread = new Thread(bot::Play);
            threads.add(thread);
            thread.start();
        }
        //play and get message from them to interrupt threads
    }

    private void setPlayers() {
        for (ClientManager clientManager : this.clientManagers) {
            join(clientManager.getName(), clientManager.getAuthToken());
        }
        for (int i = clientManagers.size(); i < playerNumber; i++) {
            String name = "bot" + (i - clientManagers.size());
            botPlayers.add(new BotPlayer(name, this));
        }
    }

    private String sendGameStatus() {
        for (ClientManager clientManager : this.clientManagers) {
            List<Integer> hand = new ArrayList<>();
            for (PlayerInfo player : this.players) {
                if (player.getToken().equals(clientManager.getAuthToken())) {
                    hand = player.getHand();
                }
            }
            return this.heartCards + " " + this.ninjaCards + " " + this.lastPlayedCard + " " + hand.toString();
        }
        return "Could not get status!!";
    }

    @Override
    public void run() {
        start();
    }
}
