package server.logic;

import server.logic.model.*;
// A Java program for a Serverside
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameController extends GameObserver implements IGameController {
    private List<TheMindGame> games;
    private TheMindGameUI ui;
    private  List<PlayerInfo> players;
    private SecureRandom random;
    private int lastBotIndex;

    public GameController(TheMindGameUI ui)
    {
        this.lastBotIndex = 0;
        this.ui = ui;
        this.random = new SecureRandom();// SecureRandom.getInstanceStrong();
        this.players = new ArrayList<PlayerInfo>();
        this.games = new ArrayList<>();
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

    public PlayerInfo GetPlayerByToken(String token) {
        for (PlayerInfo player: this.players) {
            if(player.Token.equals(token))
                return player;
        }
        return null;
    }

    @Override
    public String CreateNewGame(String hostToken, String gameName,int capacity) {
        TheMindGame game = GetGameByName(gameName);
        if (game != null) return "Invalid Name";
        PlayerInfo host = this.GetPlayerByToken(hostToken);
        if(host == null)
            return "Invalid Host";
        if(capacity < 2 || capacity >6)
            return "Invalid Capacity";
        game = new TheMindGame(gameName,this,host,capacity);
        this.games.add(game);
        game.AddObserver(this);
        return "Success";
    }

    public TheMindGame GetGameByName(String name) {
        for (TheMindGame game : this.games)
            if(game.Name.equals(name))
                return game;
        return null;
    }

    public  List<String> GetGames()
    {
        List<String> gameNames = new ArrayList<>();
        for (TheMindGame game:this.games) {
            gameNames.add(game.Name);
        }
        return gameNames;
    }


     String AddBot1(String token, String name,String gameName) {
        TheMindGame game = this.GetGameByName(gameName);
        if(game == null)
            return "Invalid game.";
        BotPlayer bot = new BotPlayer(name);

        String botToken =  Join1(bot,gameName);
        bot.Join(botToken,game);
        return "Success";
    }
    //Todo: این دیگه قدیمی شد. از اون یکی استفاده کنیم.
    @Override
    public String Join1( Player observer,String gameName)
    {
        TheMindGame game = this.GetGameByName(gameName);
        if(game == null)
            return "Invalid game";
        GameStatus status = game.getStatus();
        if(status != GameStatus.NotStarted)
            return "Invalid connecting time";
        PlayerInfo player = GetPlayerByName(observer.GetName());
        if(player != null)
            return "duplicative name";
        String token = GetUnusedToken();
        player = new PlayerInfo(token,observer);
        this.players.add(player);
         game.AddPlayer(player);
        return token;
    }
    @Override
    public String Join( String token,String gameName)
    {
        TheMindGame game = this.GetGameByName(gameName);
        if(game == null)
            return "Invalid game";
        GameStatus status = game.getStatus();
        if(status != GameStatus.NotStarted)
            return "Invalid connecting time";
        PlayerInfo player = GetPlayerByToken(token);
        if(player == null)
            return "Invalid token";
        return game.AddPlayer(player);
    }
    @Override
    public String Register(Player observer)
    {
        PlayerInfo player = GetPlayerByName(observer.GetName());
        if(player != null)
            return "duplicative name";
        String token = GetUnusedToken();
        player = new PlayerInfo(token,observer);
        this.players.add(player);
        return token;
    }


    private PlayerInfo GetPlayerByName(String name) {
        for (PlayerInfo player: this.players) {
            if(player.Name.equals(name))
                return player;
        }
        return null;
    }


    private void Log(String message) {
        this.ui.DisplayEvent( "["+ LocalDateTime.now().format(DateTimeFormatter.ISO_TIME)+"] "+message);
    }

    //Todo: سارا
    @Override
    public String StartGame(String token,  String gameName) {
        TheMindGame game = this.GetGameByName(gameName);
        if(game == null)
            return "Invalid game";
        GameStatus status = game.getStatus();
        if(status != GameStatus.NotStarted && status != GameStatus.GameOver)
            return "Invalid time to start";
        int count = game.capacity- game.GetCountOfPlayers();
        for (int i = 0 ; i < count;i++) {
            this.lastBotIndex++;
            this.AddBot1(token, "Bot" + lastBotIndex, gameName);
        }
        game.Start();
        return "Success";
    }

    @Override
    public void StatusChanged(GameStatus status) {
        this.Log("Status changed to "+status);
    }

    @Override
    public void NotifyPlaysCard(String player, Integer card) {
        this.Log(player+ " plays "+ card + ".");
    }

    @Override
    public void NotifyNinjaPropose(String player) {
        this.Log(player + " proposes Ninja.");
    }
    @Override
    public void NotifyNinjaAgreement() {
        this.Log( "Ninja Agreement.");
    }

    @Override
    public void NotifyHeartMissed() {
        this.Log("Heart missed.");
    }

    public  boolean IsOpen() {
        for (TheMindGame game : this.games) {
            GameStatus status = game.getStatus();
            if (status != GameStatus.GameOver)
                return true;
        }
        return false;
    }


    //TODO : to check if there is a 'notStarted' game between games
    public boolean isOpen() {
        for (TheMindGame game : this.games) {
            if (game.getStatus() == GameStatus.NotStarted)
                return true;
        }
        return false;
    }

    //TODO : Return an existing game
    public String joinAnExistingGame() {
        for (TheMindGame game : this.games) {
            if (game.getStatus() == GameStatus.NotStarted) {
                return game.Name;
            }
        }
        return "Game not found!";
    }

}
