package main.Utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class to format logs
 */
public class logFormatter extends Formatter {

    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    public String format(LogRecord record) {

        StringBuilder builder = new StringBuilder();
        builder.append(df.format(new Date(record.getMillis()))).append(" - ");

        builder.append("[").append(record.getLevel()).append("] - ");
        builder.append(formatMessage(record));
        builder.append("\n");
        builder.append("\n");
        return builder.toString();
    }
//      builder.append("[").append(record.getSourceClassName()).append(".");
//      builder.append(record.getSourceMethodName()).append("] - ");
}
