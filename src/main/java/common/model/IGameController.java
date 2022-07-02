package common.model;

import server.logic.TheMindGame;

import java.util.List;

public interface IGameController  {
    String createNewGame(String hostToken, String gameName, int capacity);

    TheMindGame getGameByName(String name) ;

    List<String> getGames();
    List<String> getJoinableGames();
    String joinToAvailableGame(String token);
    String join(String token, String gameName);
    String register(Player observer);

    String startGame(String token, String gameName);

    boolean isOpen() ;

}
