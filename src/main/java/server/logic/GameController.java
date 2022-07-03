package server.logic;

import server.log.ILogger;
import common.model.*;
// A Java program for a Serverside
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameController extends GameObserver implements IGameController {
    private List<TheMindGame> games;
    private ILogger logger;
    private List<PlayerInfo> players;
    private SecureRandom random;
    private int lastBotIndex;

    public GameController(ILogger logger) {
        this.lastBotIndex = 0;
        this.logger = logger;
        this.random = new SecureRandom();// SecureRandom.getInstanceStrong();
        this.players = new ArrayList<PlayerInfo>();
        this.games = new ArrayList<>();
    }

    private String getUnusedToken() {
        PlayerInfo player;
        String token;
        do {
            token = String.valueOf(this.random.nextLong());
            player = getPlayerByToken(token);
        } while (player != null);
        return token;
    }

    public PlayerInfo getPlayerByToken(String token) {
        for (PlayerInfo player : this.players) {
            if (player.Token.equals(token))
                return player;
        }
        return null;
    }

    @Override
    public String createNewGame(String hostToken, String gameName, int capacity) {
        TheMindGame game = getGameByName(gameName);
        if (game != null) return "Invalid Name";
        PlayerInfo host = this.getPlayerByToken(hostToken);
        if (host == null)
            return "Invalid Host";
        if (capacity < 2 || capacity > 8)
            return "Invalid Capacity";
        game = new TheMindGame(gameName, this, host, capacity, this.logger);
        this.games.add(game);
        game.addObserver(this);
        return "Success";
    }

    public TheMindGame getGameByName(String name) {
        for (TheMindGame game : this.games)
            if (game.getName().equals(name))
                return game;
        return null;
    }

    public List<String> getGames() {
        List<String> gameNames = new ArrayList<>();
        for (TheMindGame game : this.games) {
            gameNames.add(game.getName());
        }
        return gameNames;
    }

    String addBot(BotPlayer bot, TheMindGame game) {
        String botToken = this.register(bot);
        join(botToken, game.getName());
        bot.join(botToken, game);
        return "Success";
    }

    @Override
    public String join(String token, String gameName) {
        TheMindGame game = this.getGameByName(gameName);
        if (game == null)
            return "Invalid game";
        GameStatus status = game.getStatus();
        if (status != GameStatus.NotStarted)
            return "Invalid connecting time";
        PlayerInfo player = getPlayerByToken(token);
        if (player == null)
            return "Invalid token";
        return game.join(player);
    }

    @Override
    public String register(Player observer) {
        PlayerInfo player = getPlayerByName(observer.getName());
        if (player != null)
            return "duplicative name";
        String token = getUnusedToken();
        player = new PlayerInfo(token, observer);
        this.players.add(player);
        return token;
    }

    private PlayerInfo getPlayerByName(String name) {
        for (PlayerInfo player : this.players) {
            if (player.Name().equals(name))
                return player;
        }
        return null;
    }

    private void log(String message) {
        this.logger.log("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_TIME) + "] " + message);
    }

    @Override
    public String startGame(String token, String gameName) {
        TheMindGame game = this.getGameByName(gameName);
        if (game == null)
            return "Invalid game";
        GameStatus status = game.getStatus();
        if (status != GameStatus.NotStarted && status != GameStatus.GameOver)
            return "Invalid time to start";
        int playerCount = game.getCountOfPlayers();
        int count = game.getCapacity() - game.getCountOfPlayers();
        for (int i = 0; i < count; i++) {
            this.lastBotIndex++;
            String name = "Bot" + lastBotIndex;
            BotPlayer bot = new BotPlayer(name, playerCount);
            this.addBot(bot, game);
        }
        game.start();
        return "Success";
    }

    @Override
    public void notifyStatusChange(GameStatus status) {
        this.log("Status changed to " + status);
    }

    @Override
    public void notifyPlaysCard(String player, Integer card) {
        this.log(player + " plays " + card + ".");
    }

    @Override
    public void notifyNinjaPropose(String player) {
        this.log(player + " proposes Ninja.");
    }

    @Override
    public void notifyNinjaAgreement() {
        this.log("Ninja Agreement.");
    }

    @Override
    public void notifyHeartMissed() {
        this.log("Heart missed.");
    }

    @Override
    public void notifyJoin(String player) {
        this.log(player + " joined.");
    }

    @Override
    public void notifyLevelChange(int level) {
        this.log("Level changed to "+level);
    }

    public boolean isOpen() {
        for (TheMindGame game : this.games) {
            GameStatus status = game.getStatus();
            if (status != GameStatus.GameOver)
                return true;
        }
        return false;
    }

    public List<String> getJoinableGames() {
        List<String> names = new ArrayList();
        for (TheMindGame game : this.games) {
            if (game.isJoinable()) {
                names.add(game.getName());
            }
        }
        return names;
    }

    public String joinToAvailableGame(String token) {
        for (TheMindGame game : this.games) {
            if (game.isJoinable()) {
                String result = this.join(token, game.getName());
                if (result.equals("Success"))
                    return game.getName();
                return result;
            }
        }
        PlayerInfo player = this.getPlayerByToken(token);
        if (player == null)
            return "Invalid Player";
        String gameName = player.Name() + "Game";
        String result = createNewGame(token, gameName, 4);
        if (result.equals("Success"))
            return gameName;
        return result;
    }

    public String removeGame(String token, String gameName) {
        PlayerInfo player = this.getPlayerByToken(token);
        if (player == null)
            return "Invalid User";
        TheMindGame game = this.getGameByName(gameName);
        if (game == null)
            return "Invalid Game";
        if (game.getHostName().compareTo(player.Name()) != 0)
            return "Player is not host.";
        GameStatus status = game.getStatus();
        if (status != GameStatus.GameOver)
            game.stop();
        this.games.remove(game);
        return "Success";
    }
}
