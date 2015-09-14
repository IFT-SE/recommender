package recommendationsystem.playback;

import java.io.FileNotFoundException;

import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.views.RecommendationSystemView;

/**
 * LogPlaybackThreadDispatcher initializes and runs an instance of
 * {@link LogPlayback} in a separate thread. This allows it to interface with
 * the current instance of Eclipse without blocking when the thread goes to
 * sleep.
 * 
 * @author David Piorkowski
 * 
 */
public class LogPlaybackThreadDispatcher extends Thread
{
    private String fileName;
    private RecommendationSystemView view;
    private ErrorLogger eLog;

    /**
     * This constructor requires the log file to playback and the plug-in's
     * view.
     * 
     * @param fileName
     *            The path of the log file to play back
     * @param view
     *            The RecommendationSystemView for the plug-in
     */
    public LogPlaybackThreadDispatcher(String fileName,
            RecommendationSystemView view)
    {
        this.fileName = fileName;
        this.view = view;
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Executes an instance of LogPlayback on this thread.
     */
    @Override
    public void run()
    {
        try
        {
            LogPlayback.getInstance().runLog(fileName, view);
        }
        catch (FileNotFoundException e)
        {
            eLog.logException(e);
        }
    }
}
