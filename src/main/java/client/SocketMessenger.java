package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketMessenger implements IMessenger{
    private Socket audience;
    private IMessageListener client;
    private  Scanner in;
    private PrintWriter out;

    public SocketMessenger(IMessageListener client) {

        this.client = client;
    }

    public void listen(String message) {
        this.client.listen(message);
    }

    public void sendMessage(String message) {
        try {
            out.println(message);
            out.flush();
        } catch (Exception e){
            System.out.println("message is not sent");
        }
    }


    public void SetSocket(Socket socket) {
        this.audience = socket;

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
}
