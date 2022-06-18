package server.logic;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServerSideController {
    @FXML
    private Label welcomeText;
    @FXML
    private ListView listView;


    //initialize socket and input stream
    private List<Socket> clients = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private boolean closing;
    // constructor with port

    public ServerSideController()
    {
        this.closing = false;
        this.clients = new ArrayList<>();
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
    protected void onServerClose() {
        Log("Closing");
        int port = 5000;
// starts server and waits for a connection
        try{
// close connection
            this.closing = true;
            for (Socket socket:
                 clients)
            {
                socket.close();
            }
            in.close();
        }
        catch(IOException i){
            System.out.println(i);
            this.closing = true;
        }

    }

}
