package server.logic.model;

import server.logic.GameStatus;

public interface IGamePanel {
    GameStatus getStatus();

    int GetNinjaCards();

    String ProposeNinja(String token);

    Integer GetLastPlayedCard();

    int GetCountOfUnplayedCards();

    String Play(String token, Integer card);
}
