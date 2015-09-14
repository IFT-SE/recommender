package recommendationsystem.model.pfis;

import recommendationsystem.model.pfis.PFISNode.NodeType;

/**
 * PFISWordList is one of the three node lists used in the PFIS topology. It
 * contains a list of all the words excluding stop words that exist in the
 * source code project. Each word should only exist once in this list.
 * <p>
 * Each child of PFISWordList's nodes should be a method node in
 * {@link PFISMethodList}. During calls that request a new set of
 * recommendations, the spread of activation starts links PFISHistoryList,
 * through this and onto PFISMethodList.
 * <p>
 * This object should not be called directly. {@link PFISMatrix} uses it
 * exclusively.
 * <p>
 * PFISWordList is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISWordList extends PFISNodeList
{
    private static PFISWordList instance;

    /**
     * The singleton constructor
     */
    private PFISWordList()
    {
        // PFISWordList is singleton, do not put anything here
    }

    /**
     * Returns an instance of PFISWordList.
     * 
     * @return The singleton instance of PFISWordList
     */
    public static synchronized PFISWordList getInstance()
    {
        if (instance == null)
        {
            instance = new PFISWordList();
            instance.init();
        }
        return instance;
    }

    /**
     * Adds the given word to the list of words. This method prevents duplicates
     * from being added to the list.
     * 
     * @param word
     *            The word to add to the list
     * @return <ul>
     *         <li>A pointer to the given word's node if it does not exist in
     *         the list
     *         <li>A pointer to the pre-existing node in the list if the given
     *         node is a duplicate
     *         </ul>
     */
    public PFISNode addNode(String word)
    {
        return addNodeNoDuplicate(new PFISNode(word, NodeType.WORD));
    }
}
