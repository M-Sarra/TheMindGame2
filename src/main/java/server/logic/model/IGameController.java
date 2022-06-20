package server.logic.model;

import server.logic.TheMindGame;
import server.logic.model.BotPlayer;
import server.logic.model.GameObserver;
import server.logic.model.Player;
import server.logic.model.PlayerInfo;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public interface IGameController  {
    public abstract boolean CreateNewGame(String name);

    public abstract TheMindGame GetGameByName(String name) ;

    public abstract List<String> GetGames();

    public abstract String AddBot(String token, String name,String gameName);

    public abstract String Join( Player observer,String gameName);

    public void StartGame(String token,  String gameName);

    public  boolean IsOpen() ;

    public String joinAnExistingGame() ;

}
