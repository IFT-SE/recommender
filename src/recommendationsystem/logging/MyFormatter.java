package recommendationsystem.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * MyFormatter is used to define what format log messages get recorded for
 * {@link MyLogger}.
 * 
 * @author David Piorkowski
 * 
 */
public class MyFormatter extends Formatter
{
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS Z");

    /**
     * Return a formatted version of the record. The format is "Year-Month-Day
     * Hour:Minutes:Seconds.Milliseconds TimeZone [tab] LogMessage".
     */
    @Override
    public String format(LogRecord rec)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getFormattedDate(rec.getMillis()));
        sb.append('\t');
        sb.append(rec.getMessage());
        sb.append(System.getProperty("line.separator"));

        return sb.toString();
    }

    /**
     * Converts milliseconds to a readable date format for the
     * <code>format</code> method.
     * 
     * @param millis
     *            The time in milliseconds
     * @return The converted date string
     */
    private String getFormattedDate(long millis)
    {
        return dateFormat.format(new Date(millis));
    }

}
