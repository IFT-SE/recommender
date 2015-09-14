package recommendationsystem.playback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.eclipse.swt.widgets.Display;

import recommendationsystem.controllers.MethodManager;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.logging.MyLogger.Events;
import recommendationsystem.views.RecommendationSystemView;

/**
 * LogPlayback allows the experimenter to play back a previously recorded log to
 * confirm results or to generate a log using a different model. The playback
 * mechanism runs through the log file specified and replays events that match
 * Events.CURRENT_METHOD in 5 second intervals.
 * <p>
 * To configure and execute a playback session, do the following:
 * <ul>
 * <li>Set the log file's path in the plug-in preferences
 * <li>Select "Load Model From File" and specify the file to load from in the
 * plug-in preferences
 * <li>Select "Enable playback controls" in the plug-in preferences
 * <li>Restart Eclipse
 * <li>Wait for the model to load (You can track its progress in Eclipse's
 * progress view)
 * <li>Type in the path of the log to playback from in the empty text box above
 * the "Playback Log" button.
 * <li>Click the "Playback Log" button and wait until the navigations end.
 * </ul>
 * <b>Do not click on Eclipse's source code editor during log playback. Your
 * navigations will inadvertently be logged as well!</b>
 * <p>
 * LogPlayback is meant to be called using {@link LogPlaybackThreadDispatcher}.
 * <p>
 * LogPlayback is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class LogPlayback
{
    private static LogPlayback instance;
    private ErrorLogger eLog;

    /**
     * The singleton constructor.
     */
    private LogPlayback()
    {
        // LogPlayback is singleton, do not put anything here.
    }

    /**
     * Get the instance of LogPlayback.
     * 
     * @return The singleton instance of LogPlayback
     */
    public synchronized static LogPlayback getInstance()
    {
        if (instance == null)
        {
            instance = new LogPlayback();
            instance.init();
        }
        return instance;
    }

    /**
     * LogPlayback is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private void init()
    {
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Reads and executes the Log in the currently opened Eclipse IDE. This will
     * read the log whose file name is specified and write it to the location
     * specified in the plug-in's preferences. Each navigation will bring the
     * necessary source files into focus and use the existing models and logging
     * tools to determine recommendations. Each navigation is separated by 5
     * minute intervals.
     */
    public void runLog(String fileName, final RecommendationSystemView view)
            throws FileNotFoundException
    {
        Scanner sc = new Scanner(new FileInputStream(fileName));
        String buf = null;
        String[] tokens;
        try
        {
            while (sc.hasNextLine())
            {
                buf = sc.nextLine().trim();
                tokens = buf.split("\t");

                if (tokens[1].equals(Events.CURRENT_METHOD.toString()))
                {

                    // We can't update the view from this thread, so we have to
                    // tap into the
                    // GUI thread and update from there.
                    Display.getDefault()
                            .syncExec(new Revealer(tokens[2], view));
                    Thread.sleep(5000);
                }
            }
        }
        catch (InterruptedException e)
        {
            eLog.logException(e);
            e.printStackTrace();
        }
        finally
        {
            sc.close();
        }
    }

    /**
     * Revealer simply calls the RecommendationSystemView's reveal(String)
     * method. It is its own class since the GUI runs on its own independent
     * thread.
     * 
     * @author David Piorkowski
     * 
     */
    private class Revealer implements Runnable
    {
        private String methodKey;
        private RecommendationSystemView view;

        /**
         * The constructor.
         * 
         * @param methodKey
         *            The key of the method to reveal
         * @param view
         *            The plug-in's view that contains the reveal method
         */
        public Revealer(String methodKey, RecommendationSystemView view)
        {
            this.methodKey = methodKey;
            this.view = view;
        }

        /**
         * Calls the reveal method in the underlying view
         */
        @Override
        public void run()
        {
            view.reveal(MethodManager.getInstance()
                    .getMethodDeclarationFromMethodKey(methodKey));
        }

    }
}
