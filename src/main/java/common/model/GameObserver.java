package common.model;

public abstract class GameObserver {
    public abstract void notifyStatusChange(GameStatus status);

    public abstract void notifyPlaysCard(String player, Integer card) ;
    public abstract void notifyNinjaPropose(String player) ;
    public abstract void notifyNinjaAgreement() ;

    public abstract void notifyHeartMissed();

    public abstract void notifyJoin(String player);

    public abstract void notifyLevelChange(int level);
}
