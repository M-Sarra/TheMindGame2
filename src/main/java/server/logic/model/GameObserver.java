package server.logic.model;

import server.logic.GameStatus;

public abstract class GameObserver {
    public abstract void StatusChanged(GameStatus status);

    public abstract void NotifyPlaysCard(String player, Integer card) ;
    public abstract void NotifyNinjaPropose(String player) ;
    public abstract void NotifyNinjaAgreement() ;

    public abstract void NotifyHeartMissed();

}
