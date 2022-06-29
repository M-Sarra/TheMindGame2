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
import java.util.Observer;

public interface IGameController  {
    String CreateNewGame(String hostToken, String gameName,int capacity);

    TheMindGame GetGameByName(String name) ;

    List<String> GetGames();
    List<String> GetJoinableGames();
    String JoinToAvailableGame(String token);


    //public abstract String AddBot(String token, String name,String gameName);

    String Join(String token,String gameName);
    String Register(Player observer);

    String StartGame(String token,  String gameName);

    boolean IsOpen() ;

}
