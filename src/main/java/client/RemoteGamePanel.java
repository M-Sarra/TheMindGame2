package client;

import common.model.GameStatus;
import common.model.IGamePanel;
import common.model.Player;

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
    private int countOfNotPlayedCards;
private Player player;
    public RemoteGamePanel(Player player)
    {
        this.player = player;
        this.ninjaCards = 2;
    }

    public void setMessenger(IMessenger messenger)
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
    public int getNinjaCards() {
        this.transmitter.sendMessage("GetNinjaCards");
        return this.ninjaCards;
    }

    @Override
    public String proposeNinja(String token) {
        this.transmitter.sendMessage("ProposeNinja");
        return "Sent";
    }

    @Override
    public Integer getLastPlayedCard() {
        this.transmitter.sendMessage("GetLastPlayedCard");
        return this.lastPlayedCard;
    }

    @Override
    public int getCountOfNotPlayedCards() {
        this.transmitter.sendMessage("GetCountOfNotPlayedCards");
        return this.countOfNotPlayedCards;
    }

    @Override
    public String play(String token, Integer card) {
        this.transmitter.sendMessage("Play:"+card);
        return "Success";
    }

    @Override
    public void listen(String message) {
        if(message.startsWith("Token:"))
        {
            String token = message.substring((int)"Token:".length());
            this.AuthToken = token;
            this.player.giveToken(token);
            return;
        }
        if(message.startsWith("JoinResult:"))
        {
            return;
        }
        if(message.startsWith("Status:"))
        {
            this.status = GameStatus.valueOf(message.substring((int)"Status:".length()));
            return;
        }
        if(message.startsWith("Give "))
        {
            String part = message.substring((int)"Give ".length());
            Integer card = Integer.parseInt(part);
            this.player.giveCard(card);
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
        if(message.startsWith("CountOfNotPlayedCards:"))
        {
            String part = message.substring((int)"CountOfNotPlayedCards:".length());
            this.countOfNotPlayedCards = Integer.parseInt(part);
            return;
        }
        if(message.startsWith("Played:"))
        {
            String part = message.substring((int)"Played:".length());
            int index = part.indexOf(' ');
            Integer card = Integer.parseInt(part.substring(0,index));
            String player = part.substring(index+1);
            this.player.notifyPlaysCard(player,card);
            return;
        }
        if(message.startsWith("ProposedNinja:"))
        {
            String playerName = message.substring((int)"ProposedNinja:".length());
            this.player.notifyNinjaPropose(playerName);
            return;
        }
        if(message.startsWith("HeartMissed"))
        {
            this.player.notifyHeartMissed();
            return;
        }
        if(message.startsWith("NinjaAgreement"))
        {
            this.player.notifyNinjaAgreement();
            return;
        }
        if(message.startsWith("Joined:"))
        {
            String player = message.substring((int)"Joined:".length());
            this.player.notifyJoin(player);
            return;
        }
        throw null;
    }

    public void join(String gameName) {
        this.sendMessage("JoinTo:"+gameName);
    }

    public void register() {
        this.sendMessage("Register:"+this.player.getName());
    }
}
