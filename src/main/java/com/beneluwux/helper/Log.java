package com.beneluwux.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static final Logger logger = LoggerFactory.getLogger(Log.class);

    /**
     * Send an info message
     *
     * @param message the message
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Send a debug message
     *
     * @param message the message
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * Send an error message
     *
     * @param message the message
     */
    public static void error(String message) {
        logger.error(message);
    }
}
