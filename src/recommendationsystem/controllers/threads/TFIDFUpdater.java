package recommendationsystem.controllers.threads;

import org.eclipse.swt.widgets.Display;

import recommendationsystem.controllers.DbManager;
import recommendationsystem.controllers.ModelManager;
import recommendationsystem.logging.MyLogger;
import recommendationsystem.model.CurrentRecommendations;
import recommendationsystem.model.tfidf.TFIDFMatrix;
import recommendationsystem.views.RecommendationSystemView;

/**
 * TFIDFUpdater interfaces with {@link TFIDFMatrix}, making the calls necessary
 * to spread activation and set the recommendations and cues in
 * {@link CurrentRecommendations}. At the end of execution, the list of
 * recommendations has been refreshed including the GUI.
 * <p>
 * TFIDFUpdater is meant to be called using {@link TFIDFThreadDispatcher}.
 * <p>
 * TFIDFUpdater is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class TFIDFUpdater
{
    private static TFIDFUpdater instance;
    private MyLogger log;
    private DbManager dbManager;
    private TFIDFMatrix tfidfMatrix;
    private CurrentRecommendations currentRecs;
    private ModelManager modelManager;

    /**
     * The singleton constructor.
     */
    private TFIDFUpdater()
    {
        // TFIDFUpdater is singleton, do not put anything here.
    }

    /**
     * Get an instance of TFIDFUpdater.
     * 
     * @return The singleton instance of TFIDFUpdater
     */
    public static synchronized TFIDFUpdater getInstance()
    {
        if (instance == null)
        {
            instance = new TFIDFUpdater();
            instance.init();
        }
        return instance;
    }

    /**
     * TFIDFUpdater is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Initializes the private class variables used.
     */
    private void init()
    {
        log = MyLogger.getInstance();
        dbManager = DbManager.getInstance();
        tfidfMatrix = TFIDFMatrix.getInstance();
        currentRecs = CurrentRecommendations.getInstance();
        modelManager = ModelManager.getInstance();
    }

    /**
     * Updates the list of recommendations using the TF-IDF model. At the end of
     * this call, the list of recommendations facing the user has been updated.
     * 
     * @param methodKey
     *            The key of the current method location
     * @param view
     *            A pointer to the plugin's view
     */
    public synchronized void executeTFIDFUpdate(String methodKey,
            final RecommendationSystemView view)
    {
        // We can't update the view from this thread, so we have to tap into the
        // GUI thread and update from there.
        Display.getDefault().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                // view.setHistoryEnabled(false);
                view.clearHistoryTable();
            }
        });

        log.logCurrentLocation(methodKey);

        // Run TFIDF model with new history
        String[] newRecs = null;
        if (modelManager.getHistoryEnabled())
        {
            System.out.println("TFIDF with history called");
            newRecs = tfidfMatrix.getTopNRecommendations2HalfAndHalf(methodKey,
                    modelManager.getNumberOfRecommendations(), 10);
            // newRecs = tfidfMatrix.getTopNRecommendations2(methodKey,
            // modelManager.getNumberOfRecommendations(), 10);
        }
        else
        {
            System.out.println("TFIDF without history called");
            newRecs = tfidfMatrix.getTopNRecommendations2HalfAndHalf(methodKey,
                    modelManager.getNumberOfRecommendations(), 1);
            // newRecs = tfidfMatrix.getTopNRecommendations(methodKey,
            // modelManager.getNumberOfRecommendations());
        }
        String[][] cues = new String[newRecs.length][];

        int currentMethodId = dbManager.getMethodIdFromMethodKey(methodKey);
        int methodId = 0, i = 0;

        // Iterate over method keys and get cues for that key
        if (modelManager.getWordsEnabled())
        {
            for (String key : newRecs)
            {
                if (key == null)
                    break;
                log.logRecommendation(key, i);
                methodId = dbManager.getMethodIdFromMethodKey(key);
                // cues[i] = dbManager.getMostCommonWordsTFIDF(currentMethodId,
                // methodId);
                cues[i] = dbManager.getMostCommonWordsTFIDF2(currentMethodId,
                        methodId);
                log.logWords(cues[i]);
                i++;
            }
            currentRecs.setCues(cues);
        }
        // In the event that words are turned off, we still want to log
        // recommendations
        else
        {
            for (String key : newRecs)
            {
                if (key == null)
                    break;

                log.logRecommendation(key, i);
                i++;
            }
        }
        currentRecs.setRecommendations(newRecs);

        // We can't update the view from this thread, so we have to tap into the
        // GUI thread and update from there.
        Display.getDefault().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                view.updateHistory();
            }
        });
    }
}
