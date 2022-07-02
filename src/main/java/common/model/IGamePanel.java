package common.model;

public interface IGamePanel {
    GameStatus getStatus();

    int getNinjaCards();

    String proposeNinja(String token);

    Integer getLastPlayedCard();

    int getCountOfNotPlayedCards();

    String play(String token, Integer card);
}
