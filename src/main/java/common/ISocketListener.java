package common;

public interface ISocketListener {
    void disconnected();

    void messageReceived(String message);
}
