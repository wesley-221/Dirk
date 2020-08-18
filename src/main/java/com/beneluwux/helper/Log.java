package com.beneluwux.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy H:m:s");
    /**
     * Send a debug message
     * @param message the message
     */
    public static void debug(String message) {
        String date = format.format(new Date());

        System.out.printf("%s[%s][DEBUG] %s%s\n", ConsoleColors.RED, date, message, ConsoleColors.RESET);
    }

    /**
     * Send an info message
     * @param message the message
     */
    public static void info(String message) {
        String date = format.format(new Date());

        System.out.printf("%s[%s][INFO] %s%s\n", ConsoleColors.BLUE, date, message, ConsoleColors.RESET);
    }
}
