package recommendationsystem.model.pfis;

import java.util.ArrayList;
import java.util.Set;

import recommendationsystem.model.MethodHistory;
import recommendationsystem.model.pfis.PFISNode.NodeType;

/**
 * PFISMethodList is one of the three node lists used in the PFIS topology. It
 * is an extension of {@link PFISNodeList} which is a collection of all the
 * methods in a given source code project. It is the end point of the spreading
 * activation algorithm and from here we get our final list of recommendations.
 * The children nodes here are going to be empty.
 * <p>
 * This object should not be called directly. {@link PFISMatrix} uses it
 * exclusively.
 * <p>
 * PFISMethodList is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISMethodList extends PFISNodeList
{
    private static PFISMethodList instance;

    /**
     * The singleton constructor
     */
    private PFISMethodList()
    {
        // PFISMethodList is singleton, do not put anything here
    }

    /**
     * Returns an instance of PFISMethodList
     * 
     * @return The singleton instance of PFISMethodList
     */
    public static synchronized PFISMethodList getInstance()
    {
        if (instance == null)
        {
            instance = new PFISMethodList();
            instance.init();
        }
        return instance;
    }

    /**
     * Adds the given method to the list of methods. Duplicate methods are
     * ignored. This method returns a pointer to the node that was created when
     * the method was added to the list or a pointer to the pre-existing method
     * node if it is a duplicate.
     * 
     * @param methodKey
     *            The method's key to add
     * @return <ul>
     *         <li>A pointer to the given method's node if it does not exist in
     *         the list
     *         <li>A pointer to the pre-existing node in the list if the given
     *         node is a duplicate
     *         </ul>
     */
    public PFISNode addNode(String methodKey)
    {
        return addNodeNoDuplicate(new PFISNode(methodKey, NodeType.METHOD));
    }

    /**
     * Returns an ordered list of up to the top N methods with the highest
     * weight values. This method returns only method nodes with a weight that
     * is greater than one. The top recommendation is in the zero index.
     * 
     * @param methodKey
     *            The current location's method key
     * @param n
     *            The number of recommendations to return
     * @return A string array of method keys which represents an ordered list or
     *         recommendations. The zero index is the highest recommendation.
     *         Note that this array may be smaller than numToRecommend.
     */
    public String[] getTopNMethods(String methodKey, int n)
    {
        float tMax = 0;
        PFISNode currNode = null;

        float[] tempVal = new float[n];
        PFISNode[] tempNodes = new PFISNode[n];

        int numRecommendations = 0;
        int index = 0;

        for (int i = 0; i < n; i++)
        {
            for (PFISNode node : list)
            {
                if (!node.getName().equals(methodKey)
                        && node.getWeight() > tMax)
                {
                    currNode = node;
                    tMax = node.getWeight();
                }
            }
            if (tMax <= 1)
                break;

            currNode.setWeight(-currNode.getWeight());
            tempNodes[i] = currNode;
            tempVal[i] = tMax;
            tMax = 0;
            numRecommendations++;
        }

        String[] rv = new String[numRecommendations];

        for (PFISNode node : tempNodes)
        {
            if (node != null)
            {
                node.setWeight(-node.getWeight());
                rv[index] = node.getName();
                index++;
            }
        }

        return rv;
    }

    /**
     * Returns an ordered list of up to the top N methods with the highest
     * weight values. This method will try to return results such that half are
     * from visited methods and half are from unvisited methods. This method
     * returns only method nodes with a weight that is greater than one. The top
     * recommendation is in the zero index.
     * 
     * @param methodKey
     *            The current location's method key
     * @param n
     *            The number of recommendations to return
     * @return A string array of method keys which represents an ordered list or
     *         recommendations. The zero index is the highest recommendation.
     *         Note that this array may be smaller than numToRecommend.
     */
    public String[] getTopNMethodsHalfAndHalf(String methodKey, int n)
    {
        ArrayList<PFISNode> recs = new ArrayList<PFISNode>(n);
        Set<String> uniqueVisited = MethodHistory.getInstance()
                .getUniqueVisitedMethods();

        // Determine how many visited and unvisited methods to recommend
        // Don't include the current method, subtract 1
        int numVisited = uniqueVisited.size() - 1;
        int numUnvisited = n / 2 + n % 2;

        if (numVisited <= n / 2)
            numUnvisited = n - numVisited;
        else
            numVisited = n / 2;

        // Find recommendations
        float tMax = 0;
        PFISNode currNode = null;
        boolean visited = false;

        for (int i = 0; i < n; i++)
        {
            for (PFISNode node : list)
            {
                if (!node.getName().equals(methodKey)
                        && node.getWeight() > tMax)
                {
                    visited = uniqueVisited.contains(node.getName());
                    if ((visited && numVisited > 0)
                            || (!visited && numUnvisited > 0))
                    {
                        currNode = node;
                        tMax = node.getWeight();
                    }
                }
            }
            if (tMax <= 1)
                break;

            currNode.setWeight(-currNode.getWeight());
            recs.add(currNode);
            tMax = 0;

            if (visited)
                numVisited--;
            else
                numUnvisited--;
        }

        String[] rv = new String[recs.size()];
        int i = 0;

        // Get keys and reset the weights
        for (PFISNode node : recs)
        {
            node.setWeight(-node.getWeight());
            rv[i++] = node.getName();
        }
        return rv;
    }
}
