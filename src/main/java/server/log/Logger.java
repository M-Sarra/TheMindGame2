package server.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

public class Logger {
    String fileName;
    PrintWriter printWriter;

    public Logger(String fileName) {
        this.fileName = fileName;
        createFile();
        try {
            printWriter = new PrintWriter(this.fileName);
        } catch (FileNotFoundException ignored) {}
    }

    private void createFile() {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }
    }

    public void log(String event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String log = dtf.toString() + " " + event;
        printWriter.println(log);
        printWriter.flush();
    }

}
