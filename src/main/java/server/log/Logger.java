package server.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
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
        String log = getTime() + " " + event;
        printWriter.println(log);
        printWriter.flush();
    }

    private String getTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return currentDateTime.format(formatter);
    }

}
