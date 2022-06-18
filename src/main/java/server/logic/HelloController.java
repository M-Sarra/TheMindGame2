package server.logic;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
// A Java program for a Serverside
import java.net.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HelloController extends  GameObserver {
    @FXML
    private Label welcomeText;
    @FXML
    private ListView listView;
    private TheMindGame game;


    //initialize socket and input stream
    private List<Socket> clients = null;
    private  List<Player> players;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private boolean closing;
    // constructor with port

    public  HelloController()
    {
        this.closing = false;
        this.clients = new ArrayList<>();
        this.players = new ArrayList<>();
        this.game = new TheMindGame();
        this.players.add( new BotPlayer("Sara",game));
        this.players.add( new BotPlayer("Fateme",game));
        this.players.add( new BotPlayer("Abc",game));
        this.game.AddObserver(this);
    }

    @FXML
    protected void onServerActivation() {
        welcomeText.setText("Welcome to JavaFX Application!");
        int port = 5000;
// starts server and waits for a connection
        try{
            server = new ServerSocket(port);

            Log("Server started");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log("Waiting for a client ...");
                    while (!closing)
                    {
                        try {
                            Socket socket = server.accept();
                            Log("Client accepted");
                            clients.add(socket);
// takes input from the client socket
                            in = new DataInputStream(
                                    new BufferedInputStream(socket.getInputStream()));
                            String line = "";
// reads message from client until "Over" is sent
                            while (!line.equals("Over") && !closing)
                            {
                                try
                                {
                                    byte[] bytes = new byte[1000];
                                    int count = in.read(bytes);
                                    String temp = new String(bytes);
                                    line = temp;// String.valueOf(count);// bytes in.read().readUTF();
                                    Log(line);

                                }
                                catch(IOException i)
                                {
                                    Log(i.getMessage());
                                }
                            }
                            System.out.println("Closing connection");
                        }
                        catch (Exception ex)
                        {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            });
            thread.start();
// close connection
        }
        catch(IOException i){
            System.out.println(i);
            this.closing = true;
        }

    }

    private void Log(String message) {
        Platform.runLater(()->
        this.listView.getItems().add(0, "["+ LocalDateTime.now().format(DateTimeFormatter.ISO_TIME)+"]"+message)
        );
    }

    @FXML
    protected void onClientActivation() {
        ClientSide client = new ClientSide("127.0.0.1", 5000);
    }
    @FXML
    protected void onStartGame() {
        this.game.start();
    }

    @Override
    public void StatusChanged(GameStatus status) {
        this.Log("Status changed to "+status);
    }

    @Override
    public void GiveCard(Integer card) {

    }

    @Override
    public void NotifyPlaysCard(String player, Integer card) {
        this.Log(player+ " plays "+ card + ".");
    }

    @Override
    public void NotifyHeartMissed() {
        this.Log("Heart missed.");
    }


    public class ClientSide
    {
        // initialize socket and input output streams
        private Socket socket = null;
        private DataInputStream input1 = null;
        private DataOutputStream out = null;
        // constructor to put ip address and port
        public ClientSide(String address, int port)
        {
// establish a connection
            try
            {
                socket = new Socket(address, port);
                System.out.println("Connected");
// takes input from terminal
                input1 = new DataInputStream(System.in);
// sends output to the socket
                out = new DataOutputStream(socket.getOutputStream());
            }
            catch(UnknownHostException u)
            {
                System.out.println(u);
            }
            catch(IOException i)
            {
                System.out.println(i);
            }// string to read message from input
            try
            {

                out.writeUTF("سلام سرور");
                out.writeUTF("این متن رو برای تست فرستادم");
                out.writeUTF("ممنون میشم نمایشش بدی");
                out.writeUTF("فعلاً خداحافظ");
                out.writeUTF("Over");
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
// close the connection
            try
            {
                out.close();
                socket.close();
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }
    }

}
