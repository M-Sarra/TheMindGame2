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

    public GameController(TheMindGameUI ui)
    {
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
    public boolean CreateNewGame(String name) {
        TheMindGame game = GetGameByName(name);
        if (game != null) return false;
        game = new TheMindGame(name,this);
        this.games.add(game);
        game.AddObserver(this);
        return true;
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

    @Override
    public String AddBot(String token, String name,String gameName) {
        TheMindGame game = this.GetGameByName(gameName);
        if(game == null)
            return "Invalid game.";
        BotPlayer bot = new BotPlayer(name);
        String botToken =  Join(bot,gameName);
        bot.Join(botToken,game);
        return "Success";
    }
    //برای اتصال به بازی باید یک Player رو به این متد بدیم
    //وقتی که کلاینت مون همون بازیکن هست باید اینجا ClientManager رو بهش بدیم
    @Override
    public String Join( Player observer,String gameName)
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
    public void StartGame(String token,  String gameName) {
        TheMindGame game = this.GetGameByName(gameName);
        if(game == null)
            return;
        game.Start();
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
