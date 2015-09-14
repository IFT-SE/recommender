package recommendationsystem.controllers.threads;

import recommendationsystem.views.RecommendationSystemView;

/**
 * PFISThreadDispatcher is a threaded class that is used to run an iteration of
 * the PFIS model when the user enters a new method. It runs in a separate
 * thread to prevent the Eclipse from blocking until the update is done. Upon
 * completion, it will make the calls to the view to update the tables
 * accordingly.
 * <p>
 * PFISThreadDispatcher uses the singleton class {@link PFISUpdater} as its
 * synchronization mechanism. This class simply executes allows updates to
 * happen on their own thread.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISThreadDispatcher extends Thread
{
    private String methodKey;
    private RecommendationSystemView view;

    /**
     * PFISUpdater constructor.
     * 
     * @param methodKey
     *            The current location's method's key. This should never be
     *            null.
     * @param view
     *            The RecommendationSystemView to update.
     */
    public PFISThreadDispatcher(String methodKey, RecommendationSystemView view)
    {
        this.methodKey = methodKey;
        this.view = view;
    }

    /**
     * Execute the thread. The following things happen during execution:
     * <ul>
     * <li>The current location is added to the PFISMatrix.
     * <li>Spreading activation occurs over the PFISMatrix.
     * <li>The top N recommendations are extracted.
     * <li>The most visited word nodes are determined.
     * <li>The CurrentRecommendations object is updated according to the model's
     * results.
     * <li>The RecommendationSystemView's table of recommendations is told to
     * update.
     * </ul>
     * <p>
     * See {@link PFISUpdater} for details.
     */
    @Override
    public void run()
    {
        PFISUpdater.getInstance().executePFISUpdate(methodKey, view);
    }
}
