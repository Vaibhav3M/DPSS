package main.Utilities;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Custom logger.
 */
public class CustomLogger {

    /** fileHandler for logging to file */
    static private FileHandler fileHandler;

    /**
     * Sets .
     *
     * @param logFile the log file
     * @return the
     * @throws IOException the io exception
     */
    public static FileHandler setup(final String logFile) throws IOException {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setUseParentHandlers(false); // don't log the console output to file
        logger.setLevel(Level.INFO);
        fileHandler = new FileHandler(logFile, true);
        fileHandler.setFormatter(new logFormatter());
        logger.addHandler(fileHandler);

        return fileHandler;
    }
}