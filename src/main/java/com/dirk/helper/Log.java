package com.dirk.helper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Log {

    /**
     * Send an info message
     *
     * @param message the message
     */
    public static void info(String message) {
        log.info(message);
    }

    /**
     * Send a debug message
     *
     * @param message the message
     */
    public static void debug(String message) {
        log.debug(message);
    }

    /**
     * Send an error message
     *
     * @param message the message
     */
    public static void error(String message) {
        log.error(message);
    }
}
