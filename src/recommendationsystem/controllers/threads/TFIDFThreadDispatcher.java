package recommendationsystem.controllers.threads;

import recommendationsystem.views.RecommendationSystemView;

/**
 * TFIDFThreadDispatcher is a threaded class that is used to run an iteration of
 * the TFIDF model when the user enters a new method. It runs in a separate
 * thread to prevent the Eclipse from blocking until the update is done. Upon
 * completion, it will make the calls to the view to update the tables
 * accordingly.
 * 
 * @author David Piorkowski
 * 
 */
public class TFIDFThreadDispatcher extends Thread
{
    private String methodKey;
    private RecommendationSystemView view;

    /**
     * TFIDFThreadDispatcher constructor.
     * 
     * @param methodKey
     *            The current location's method's key. This should never be
     *            null.
     * @param view
     *            The RecommendationSystemView to update.
     */
    public TFIDFThreadDispatcher(String methodKey, RecommendationSystemView view)
    {
        this.methodKey = methodKey;
        this.view = view;
    }

    /**
     * Execute the thread. The following things happen during execution:
     * <ul>
     * <li>The top N recommendations are extracted from TFIDFMatrix based on the
     * current location.
     * <li>The most common words between the current method and each
     * recommendation are determined.
     * <li>The CurrentRecommendations object is updated according to the model's
     * results.
     * <li>The RecommendationSystemView's table of recommendations is told to
     * update.
     * </ul>
     */
    @Override
    public void run()
    {
        TFIDFUpdater.getInstance().executeTFIDFUpdate(methodKey, view);
    }

}
