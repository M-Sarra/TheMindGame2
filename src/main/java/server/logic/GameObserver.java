package server.logic;

public abstract class GameObserver {
    public abstract void StatusChanged(GameStatus status);

    public abstract void GiveCard(Integer card) ;

    public abstract void NotifyPlaysCard(String player, Integer card) ;

    public abstract void NotifyHeartMissed();

}
