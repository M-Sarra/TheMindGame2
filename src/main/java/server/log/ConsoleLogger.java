package server.log;

public class ConsoleLogger implements  ILogger{

    public void log(String message) {
        synchronized (this) {
            System.out.println(message);
        }
    }
}
