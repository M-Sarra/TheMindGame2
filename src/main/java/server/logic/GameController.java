package server.logic;

import server.logic.model.BotPlayer;
import server.logic.model.GameObserver;
import server.logic.model.Player;
// A Java program for a Serverside
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameController extends GameObserver {
    private TheMindGame game;
    private TheMindGameUI ui;
    private  List<Player> players;

    public GameController(TheMindGameUI ui)
    {
        this.ui = ui;
        this.players = new ArrayList<Player>();
        this.game = new TheMindGame();
        //Todo: اضافه شدن بات ها در جای مناسبتری قرار گیرد
        this.game.AddObserver(this);
        AddBot("Sara");
        AddBot("Fateme");
        AddBot("Abc");
    }

    private void AddBot(String name) {
        BotPlayer bot = new BotPlayer(name, game);
        this.players.add(bot);
    }

    private void Log(String message) {
        this.ui.DispayEvent( "["+ LocalDateTime.now().format(DateTimeFormatter.ISO_TIME)+"] "+message);
    }

    //Todo: سارا
    public void StartGame() {
        this.game.Start();
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
    public void NotifyHeartMissed() {
        this.Log("Heart missed.");
    }

    public  boolean IsOpen()
    {
        GameStatus status =  this.game.getStatus();
        return (status != GameStatus.GameOver);
    }
}
