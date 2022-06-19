package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MessageTransmitter {
    private final Scanner in;
    private final PrintWriter out;

    public MessageTransmitter(Socket socket) {
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

    public String getMessage() {
        try {
            return in.nextLine();
        } catch (Exception e) {
            return "Could not get message from server!!";
        }
    }

    public void sendMessage(String message) {
        try {
            out.println(message);
            out.flush();
        } catch (Exception ignored){}
    }


}
