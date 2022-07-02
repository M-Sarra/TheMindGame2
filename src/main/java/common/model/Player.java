package common.model;

public abstract class Player extends GameObserver {
    public abstract String getName();
    public abstract void giveCard(Integer card) ;

    public abstract void giveToken(String token);
}
