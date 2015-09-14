package recommendationsystem.model;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import recommendationsystem.controllers.MethodManager;
import recommendationsystem.controllers.threads.PFISThreadDispatcher;
import recommendationsystem.controllers.threads.TFIDFThreadDispatcher;
import recommendationsystem.providers.RecommendationsContentProvider;

/**
 * CurrentRecommendations is the data source for the viewer used to display the
 * recommendations. Both the {@link PFISThreadDispatcher} and the
 * {@link TFIDFThreadDispatcher} update this object to store the recommendations
 * that they calculate. {@link RecommendationsContentProvider} reads this class
 * to display the recommendations to the user in the plug-in's view. Each
 * method-to-method navigation updates this object through the
 * <code>setRecommendations(MethodDeclaration[])</code> and
 * <code>setCues(String[]) methods.</code>.
 * <p>
 * This class has two main components, an array of MethodDeclaration nodes and
 * an array of string arrays which represent the cues for each of the
 * MethodDeclaration nodes. For each method there is an array of words
 * representing its cues where cues[i] belongs to currentRecs[i].
 * <p>
 * CurrentRecommendations is a singleton class. Call <code>getInstance()</code>
 * to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class CurrentRecommendations
{
    private static CurrentRecommendations instance;
    private MethodDeclaration[] currentRecs;
    private MethodManager methodManager;
    private String[][] cues;

    /**
     * The singleton constructor.
     */
    private CurrentRecommendations()
    {
        // This class is singleton, do not put anything here
    }

    /**
     * Gets the instance of CurrentRecommendations.
     * 
     * @return A singleton instance of CurrentRecommendations
     */
    public static synchronized CurrentRecommendations getInstance()
    {
        if (instance == null)
        {
            instance = new CurrentRecommendations();
            instance.init();
        }
        return instance;
    }

    /**
     * CurrentRecommendations is singleton. Don't allow clones.
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
    public void init()
    {
        methodManager = MethodManager.getInstance();
        currentRecs = new MethodDeclaration[0];
        cues = new String[0][0];
    }

    /**
     * Sets the list of methods to recommend to the user.
     * 
     * @param reccomendationMethodKeys
     *            An ordered array (where index 0 is the highest recommendation)
     *            of methods' keys to recommend to the user.Each cell contains
     *            either:
     *            <ul>
     *            <li>A MethodDeclaration representing the recommendation
     *            <li>null for a blank recommendation
     *            </ul>
     */
    public synchronized void setRecommendations(
            String[] reccomendationMethodKeys)
    {
        currentRecs = new MethodDeclaration[reccomendationMethodKeys.length];
        for (int i = 0; i < reccomendationMethodKeys.length; i++)
        {
            currentRecs[i] = methodManager
                    .getMethodDeclarationFromMethodKey(reccomendationMethodKeys[i]);
        }
    }

    /**
     * Returns the ordered array of recommendations.
     * 
     * @return An ordered array (where 0 is the highest recommendation) of
     *         MethodDeclarations. Each cell contains either:
     *         <ul>
     *         <li>A MethodDeclaration representing the recommendation
     *         <li>null for a blank recommendation
     *         </ul>
     */
    public MethodDeclaration[] getCurrentRecommendations()
    {
        return currentRecs;
    }

    /**
     * Sets the ordered array of ordered cues.
     * 
     * @param cues
     *            An array of string arrays where each string array is a list of
     *            cues that corresponds to a recommendation. The list of cues in
     *            the second array are ordered where the 0 index is the highest
     *            weighted cue. Each cell in each string array can contain:
     *            <ul>
     *            <li>A word representing the cue
     *            <li>null for no cue
     *            </ul>
     */
    public synchronized void setCues(String[][] cues)
    {
        this.cues = cues;
    }

    /**
     * Returns the ordered array of ordered cues.
     * 
     * @param cues
     *            An array of string arrays where each string array is a list of
     *            cues that corresponds to a recommendation. The list of cues in
     *            the second array are ordered where the 0 index is the highest
     *            weighted cue. Each cell in each string array can contain:
     *            <ul>
     *            <li>A word representing the cue
     *            <li>null for no cue
     *            </ul>
     */
    public String[][] getCues()
    {
        return cues;
    }
}
