import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class LoggerFormat extends Formatter {
    // Create a DateFormat to format the logger timestamp.
    //private static final DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append("[").append(record.getSourceClassName()).append(".");
        builder.append(record.getSourceMethodName()).append("] - ");
       // builder.append(df.format(new Date(record.getMillis()))).append("-");
        builder.append("[").append(record.getLevel()).append("] -> ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}