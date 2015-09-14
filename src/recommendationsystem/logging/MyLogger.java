package recommendationsystem.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import recommendationsystem.controllers.ModelManager.ModelType;

/**
 * MyLogger provides an interface for logging events that occurred in the
 * models. It logs events out to a text file specified by the
 * <code>setFileName(String)</code> method which must be set before any logging
 * occurs.
 * <p>
 * The log call order is expected to be as follows for each method navigation:
 * <ul>
 * <li>logCurrentMethod(current method key)
 * <li>logRecommendation(1st recommendation key, 0)
 * <li>logWords(1st recommendation cues)
 * <li>logRecommendation(2nd recommendation key, 1)
 * <li>logWords(2nd recommendation cues)
 * <li>logRecommendation(3rd recommendation key, 2)
 * <li>logWords(3rd recommendation cues)
 * <li>...
 * <li>logRecommendation(nth recommendation key, n-1)
 * <li>logWords(nth recommendation cues)
 * </ul>
 * However, events that occur during logging such as navigations may cause the
 * log to have the events interspersed with the call order above.
 * <p>
 * MyLogger is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class MyLogger
{
    /**
     * Contains the list of events that are recognized by the logger. Each event
     * has a corresponding log method available. This data format allows the
     * mapping of each event's shorthand code to be mapped to a reusable enum.
     * 
     */
    public enum Events
    {
        TEXT_CURSOR_CHANGED("cursor"), CURRENT_METHOD("current"), RECOMMENDED_METHOD(
                "rec"), RECOMMENDED_WORDS("words"), METHOD_OPENED("doubleclick"), PINNED_METHOD_ADDED(
                "pinned"), PINNED_METHOD_DELETED("deleted"), MODEL_SET("model");

        private String logKey;

        /**
         * Constructor.
         * 
         * @param key
         *            The key as it appears in the final log file
         */
        Events(String key)
        {
            this.logKey = key;
        }

        /**
         * Returns a string representation of the short code for this log event
         * enum.
         */
        @Override
        public String toString()
        {
            return logKey;
        }
    }

    private static MyLogger instance;
    private Logger log;
    private ErrorLogger eLog;
    private FileHandler fileHandler;
    private MyFormatter formatter;
    private String fileName;

    /**
     * The singleton constructor.
     */
    private MyLogger()
    {
        // This class is singleton. Do not put anything here.
    }

    /**
     * Get the instance of MyLogger.
     * 
     * @return A singleton instance of MyLogger
     */
    public synchronized static MyLogger getInstance()
    {
        if (instance == null)
        {
            instance = new MyLogger();
        }
        return instance;
    }

    /**
     * MyLogger is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Initializes the private class variables used. This method should not be
     * called externally.
     */
    private void init()
    {
        try
        {
            eLog = ErrorLogger.getInstance();
            log = Logger.getLogger(MyLogger.class.getName());
            log.setLevel(Level.INFO);
            fileHandler = new FileHandler(fileName);
            formatter = new MyFormatter();
            fileHandler.setFormatter(formatter);
            log.addHandler(fileHandler);
        }
        catch (SecurityException e)
        {
            eLog.logException(e);
        }
        catch (IOException e)
        {
            eLog.logException(e);
        }
    }

    /**
     * Set the file to log to. This call is required before the log is
     * initialized.
     * 
     * @param fileName
     *            The path to the file to log to.
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
        init();
    }

    /**
     * Log the current text cursor position and file name.
     * 
     * @param offset
     *            The text cursor offset in number of characters
     * @param fileName
     *            The name of the file the text cursor is in
     */
    public void logTextCursorChange(int offset, String fileName)
    {
        // TODO consider adding a full path here
        StringBuilder sb = new StringBuilder();
        sb.append(Events.TEXT_CURSOR_CHANGED);
        sb.append('\t');
        sb.append(offset);
        sb.append('\t');
        sb.append(fileName);
        log.info(sb.toString());
    }

    /**
     * Log the current method location.
     * 
     * @param methodKey
     *            The current method's key
     */
    public void logCurrentLocation(String methodKey)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Events.CURRENT_METHOD);
        sb.append('\t');
        sb.append(methodKey);
        log.info(sb.toString());
    }

    /**
     * Log the current recommendation with the given rank.
     * 
     * @param rec
     *            The current recommendation's method key
     * @param rank
     *            The current recommendation's rank
     */
    public void logRecommendation(String rec, int rank)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Events.RECOMMENDED_METHOD);
        sb.append(rank);
        sb.append('\t');
        sb.append(rec);
        log.info(sb.toString());
    }

    /**
     * Log a list of cues (words).
     * 
     * @param words
     *            A string array of words
     */
    public void logWords(String[] words)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Events.RECOMMENDED_WORDS);
        for (String word : words)
        {
            sb.append('\t');
            sb.append(word);
        }
        log.info(sb.toString());
    }

    /**
     * Log when a method is double-clicked and the user has navigated using the
     * tool.
     * 
     * @param methodKey
     *            The method's key of the destination
     */
    public void logDoubleClickAction(String methodKey)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Events.METHOD_OPENED);
        sb.append('\t');
        sb.append(methodKey);
        log.info(sb.toString());
    }

    /**
     * Log when a method is pinned in the pinned methods list.
     * 
     * @param methodKey
     *            The pinned method's key
     */
    public void logMethodPinned(String methodKey)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Events.PINNED_METHOD_ADDED);
        sb.append('\t');
        sb.append(methodKey);
        log.info(sb.toString());
    }

    /**
     * Log when a method is deleted from the pinned methods list.
     * 
     * @param methodKey
     *            The deleted method's key
     */
    public void logMethodDeleted(String methodKey)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Events.PINNED_METHOD_DELETED);
        sb.append('\t');
        sb.append(methodKey);
        log.info(sb.toString());
    }

    /**
     * Log when a new model is selected for generating recommendations.
     * 
     * @param type
     *            The type of model selected
     * @param history
     *            <ul>
     *            <li>True is history is enabled
     *            <li>False otherwise
     *            </ul>
     */
    public void logModelUsed(ModelType type, boolean history)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Events.MODEL_SET);
        sb.append('\t');
        sb.append(type.toString());
        sb.append('\t');
        if (history)
            sb.append("hist");
        else
            sb.append("noHist");
        log.info(sb.toString());
    }
}
