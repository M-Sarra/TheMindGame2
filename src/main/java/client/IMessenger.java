package client;

public interface IMessenger extends IMessageListener {

    void sendMessage(String message);
}
