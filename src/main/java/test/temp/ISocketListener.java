package test.temp;

public interface ISocketListener {
    void disconnected();

    void messageReceived(String message);
}
