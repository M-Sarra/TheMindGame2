package client.UI;

import java.util.Scanner;

public class ConsoleManager {

    public String getMessage() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public void sendMessage(String message) {
        System.out.println(message);
    }

}
