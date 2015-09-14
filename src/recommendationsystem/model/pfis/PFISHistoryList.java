package recommendationsystem.model.pfis;

import recommendationsystem.model.MethodHistory;
import recommendationsystem.model.pfis.PFISNode.NodeType;

/**
 * PFISHistoryList is one of the three node lists used in the PFIS topology. It
 * should contain the same methods and order as {@link MethodHistory}, but it is
 * up to the developer to keep the two synchronized. This object contains a list
 * of {@link PFISNode} types which represent a history of method-to-method
 * navigations, where the last node is the most recent.
 * <p>
 * Each child of PFISHistoryList's nodes should be a word node in
 * {@link PFISWordList}. Unlike the other implementations of PFISNodeList, this
 * one is allowed to have multiple instances of the same node. During calls that
 * request a new set of recommendations, the spread of activation starts here.
 * <p>
 * This object should not be called directly. {@link PFISMatrix} uses it
 * exclusively.
 * <p>
 * PFISHistoryList is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISHistoryList extends PFISNodeList
{
    private static PFISHistoryList instance;

    /**
     * The singleton constructor
     */
    private PFISHistoryList()
    {
        // PFISHistoryList is singleton, do not put anything here.
    }

    /**
     * Get the instance of PFISHistoryList.
     * 
     * @return The singleton instance of PFISHistoryList
     */
    public static synchronized PFISHistoryList getInstance()
    {
        if (instance == null)
        {
            instance = new PFISHistoryList();
            instance.init();
        }
        return instance;
    }

    /**
     * Decays the list of history nodes by the given factor. The most current
     * method's weight is untouched, but each successive method is decayed by a
     * the given factor. This is typically done before spreading activation.
     * 
     * @param decay
     *            The factor to decay by
     * @param numNodes
     *            The number of nodes to decay
     */
    public void decayList(float decay, int numNodes)
    {
        // The last node is the most recent, so we want to start there.
        // Start decaying at the second node, but include the first one in
        // numNodes.
        for (int i = list.size() - 2; numNodes > 1 && i > -1; i--, numNodes--)
        {
            PFISNode node = list.get(i);
            node.decayWeight(decay);
            decay *= decay;
        }
    }

    /**
     * Adds a node to the history and returns a pointer to it. This is the
     * method used to maintain a history list in the PFIS topology. This method
     * always appends to the end of the list.
     * 
     * @param methodKey
     *            The method's key to add
     * @return A pointer to the PFISNode that was created
     */
    public PFISNode addNode(String methodKey)
    {
        return addNode(new PFISNode(methodKey, NodeType.METHOD));
    }

    /**
     * Spread activation two steps using the given decay value which should be >
     * 0 and < 1. From history nodes to their children (which should be word
     * nodes) and then from the activated children to the grandchildren (which
     * should be method nodes). This method specifies the maximum number of
     * nodes to spread from as a performance consideration. The spreading of
     * activation starts with the most recent node first.
     * 
     * @param decay
     *            The amount of decay per iteration
     * @param numNodes
     *            The number of nodes to spread from
     */
    public void spreadActivation(float decay, int numNodes)
    {
        for (int i = list.size() - 1; numNodes > 0 && i > -1; i--, numNodes--)
        {
            PFISNode node = list.get(i);
            for (PFISNode child : node.getChildren())
            {
                child.spreadActivation(node.getWeight(), decay);
                for (PFISNode grandchild : child.getChildren())
                {
                    grandchild.spreadActivation(child.getWeight(), decay);
                }
            }

        }
    }
}
