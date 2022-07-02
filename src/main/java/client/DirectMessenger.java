package client;

public class DirectMessenger implements IMessenger,IMessageListener{
    private IMessageListener audience;
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


    public void SetAudience(IMessageListener audience) {
        this.audience = audience;
    }
    public void SetClient(IMessageListener client) {
        this.client = client;
    }
}
