package client;

public class DirectMessenger implements IMessenger{
    private IMessenger audience;
    private IMessageListener client;
    public DirectMessenger(IMessageListener client) {
        this.client = client;
    }

    public void listen(String message) {
        this.client.listen(message);
    }

    public void sendMessage(String message) {
        this.audience.listen(message);
    }


    public void SetAudience(IMessenger messenger) {
        this.audience = messenger;
    }
}
