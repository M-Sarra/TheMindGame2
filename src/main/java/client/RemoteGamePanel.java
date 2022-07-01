package client;

import server.logic.GameStatus;
import server.logic.model.IGamePanel;
import server.logic.model.Player;
import server.logic.model.PlayerInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

public class RemoteGamePanel implements IGamePanel,IMessageListener {
    private String host;
    private int port;
    private String name;
    private String AuthToken;
    private IMessenger transmitter;
    private GameStatus status;
    private int ninjaCards;
    private Integer lastPlayedCard;
    private int countOfUnplayedCards;
private Player player;
    public RemoteGamePanel(Player player)
    {
        this.player = player;
        this.ninjaCards = 2;
    }

    public void SetMessenger(IMessenger messenger)
    {
        this.transmitter = messenger;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthToken(String authToken) {
        AuthToken = authToken;
    }

    public String getName() {
        return name;
    }


    public String getAuthToken() {
        return AuthToken;
    }

    public void start() {
        setHostAndPort();
        try {
            Socket socket = new Socket(host, port);
            this.transmitter = new Messenger(socket);
            GameAgent gameManager = new GameAgent( this);
            gameManager.startGame();
        } catch (IOException e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            start();
        }
    }

    private void setHostAndPort() {
        try {
            Properties prop = new Properties();
            FileInputStream ip = new FileInputStream("src/main/java/client/file/client.properties");
            prop.load(ip);
            host = prop.getProperty("host");
            port = Integer.parseInt(prop.getProperty("port"));
        } catch (IOException e) {
            host = "localhost";
            port = 8000;
        }
    }

    public void sendMessage(String message) {
        this.transmitter.sendMessage(message);
    }

    @Override
    public GameStatus getStatus()
    {
        this.transmitter.sendMessage("GetStatus");
        return this.status;
    }

    @Override
    public int GetNinjaCards() {
        this.transmitter.sendMessage("GetNinjaCards");
        return this.ninjaCards;
    }

    @Override
    public String ProposeNinja(String token) {
        this.transmitter.sendMessage("ProposeNinja");
        return "Sent";
    }

    @Override
    public Integer GetLastPlayedCard() {
        this.transmitter.sendMessage("GetLastPlayedCard");
        return this.lastPlayedCard;
    }

    @Override
    public int GetCountOfUnplayedCards() {
        this.transmitter.sendMessage("GetCountOfUnplayedCards");
        return this.countOfUnplayedCards;
    }

    @Override
    public String Play(String token, Integer card) {
        this.transmitter.sendMessage("Play:"+card);
        return "Success";
    }

    @Override
    public void listen(String message) {
        if(message.startsWith("Status:"))
        {
            this.status = GameStatus.valueOf(message.substring((int)"Status:".length()));
            return;
        }
        if(message.startsWith("Give "))
        {
            String part = message.substring((int)"Give ".length());
            Integer card = Integer.parseInt(part);
            this.player.GiveCard(card);
            return;
        }
        if(message.startsWith("CountOfNinjaCards:"))
        {
            String part = message.substring((int)"CountOfNinjaCards:".length());
            this.ninjaCards = Integer.parseInt(part);
            return;
        }
        if(message.startsWith("LastPlayedCardIs:"))
        {
            String part = message.substring((int)"LastPlayedCardIs:".length());
            this.lastPlayedCard = Integer.parseInt(part);
            return;
        }
        if(message.startsWith("CountOfUnplayedCards:"))
        {
            String part = message.substring((int)"CountOfUnplayedCards:".length());
            this.countOfUnplayedCards = Integer.parseInt(part);
            return;
        }
        if(message.startsWith("Played:"))
        {
            String part = message.substring((int)"Played:".length());
            int index = part.indexOf(' ');
            Integer card = Integer.parseInt(part.substring(0,index));
            String player = part.substring(index+1);
            this.player.NotifyPlaysCard(player,card);
            return;
        }
        if(message.startsWith("ProposedNinja:"))
        {
            String playerName = message.substring((int)"ProposedNinja:".length());
            this.player.NotifyNinjaPropose(playerName);
            return;
        }
        if(message.startsWith("HeartMissed"))
        {
            this.player.NotifyHeartMissed();
            return;
        }
        if(message.startsWith("NinjaAgreement"))
        {
            this.player.NotifyNinjaAgreement();
            return;
        }
        throw null;
    }
}
