package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Messenger implements  IMessenger {
    private final Scanner in;
    private final PrintWriter out;
    private String AuthToken;
    int time = 0;

    public Messenger(Socket socket) {
        Scanner in1;
        PrintWriter out1;
        try {
            in1 = new Scanner(socket.getInputStream());
            out1 = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            in1 = null;
            out1 = null;
            e.printStackTrace();
        }
        in = in1;
        out = out1;
    }

    public void setAuthToken(String authToken) {
        AuthToken = authToken;
    }

    public String getMessage() {
        Timer timer = new Timer();
        String message;
        try {
            message =  in.nextLine();
        } catch (Exception e) {
            this.time++;
            if (time > 100) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getMessage();
                    }
                }, 100);
            }
            message = "Could not get message from server!!";

        }
        return message;
    }

    public void sendMessage(String message) {
        try {
            out.println(message);
            out.flush();
        } catch (Exception e){
            System.out.println("message is not sent");
        }
    }

    public void sendMessageByToken(String message) {
        String newMessage = this.AuthToken + " " + message;
        sendMessage(newMessage);
    }


    @Override
    public void listen(String message) {

    }
}
