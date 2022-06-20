package server.logic.model;

public abstract class Player extends GameObserver {
    public abstract String GetName();
    public abstract void GiveCard(Integer card) ;
}
