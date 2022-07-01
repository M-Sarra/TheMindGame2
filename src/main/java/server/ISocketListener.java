package server;

public interface ISocketListener {
    void Disconnected();

    void MessageRecived(String message);
}
