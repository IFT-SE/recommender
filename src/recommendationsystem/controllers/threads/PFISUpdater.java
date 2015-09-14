package recommendationsystem.controllers.threads;

import org.eclipse.swt.widgets.Display;

import recommendationsystem.controllers.DbManager;
import recommendationsystem.controllers.ModelManager;
import recommendationsystem.logging.MyLogger;
import recommendationsystem.model.CurrentRecommendations;
import recommendationsystem.model.pfis.PFISMatrix;
import recommendationsystem.views.RecommendationSystemView;

/**
 * PFISUpdater interfaces with {@link PFISMatrix}, making the calls necessary to
 * spread activation and set the recommendations and cues in
 * {@link CurrentRecommendations}. At the end of execution, the list of
 * recommendations has been refreshed including the GUI.
 * <p>
 * PFISUpdater is meant to be called using {@link PFISThreadDispatcher}.
 * <p>
 * PFISUpdater is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISUpdater
{
    private static PFISUpdater instance;
    private MyLogger log;
    private DbManager dbManager;
    private PFISMatrix pfisMatrix;
    private CurrentRecommendations currentRecs;
    private ModelManager modelManager;

    /**
     * The singleton constructor.
     */
    private PFISUpdater()
    {
        // This class is singleton, do not put anything here.
    }

    /**
     * Get an instance of PFISUpdater.
     * 
     * @return The singleton instance of PFISUpdater
     */
    public static synchronized PFISUpdater getInstance()
    {
        if (instance == null)
        {
            instance = new PFISUpdater();
            instance.init();
        }
        return instance;
    }

    /**
     * PFISUpdater is singleton. Don't allow clones.
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
        pfisMatrix = PFISMatrix.getInstance();
        currentRecs = CurrentRecommendations.getInstance();
        modelManager = ModelManager.getInstance();
    }

    /**
     * Updates the list of recommendations using the PFIS model. At the end of
     * this call, the list of recommendations facing the user has been updated.
     * 
     * @param methodKey
     *            The key of the current method location
     * @param view
     *            A pointer to the plugin's view
     */
    public synchronized void executePFISUpdate(String methodKey,
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

        // Run PFIS model with new history
        pfisMatrix.addToHistory(methodKey);

        String[] newRecs = null;
        if (modelManager.getHistoryEnabled())
        {
            System.out.println("PFIS with history called");
            newRecs = pfisMatrix.getTopNRecommendations(methodKey,
                    modelManager.getNumberOfRecommendations(), 10);
        }
        else
        {
            System.out.println("PFIS without history called");
            newRecs = pfisMatrix.getTopNRecommendations(methodKey,
                    modelManager.getNumberOfRecommendations(), 1);
        }
        String[][] cues = new String[newRecs.length][];

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
                // TODO: This is not the correct list of words
                // cues[i] = dbManager.getMostCommonWordsPFIS(methodId);
                cues[i] = dbManager.getMostCommonWordsPFIS2(methodId);

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

        // We can't update the view from this thread, so we have to tap into
        // the GUI thread and update from there.
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
