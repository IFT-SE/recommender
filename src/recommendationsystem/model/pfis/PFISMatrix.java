package recommendationsystem.model.pfis;

import java.sql.ResultSet;
import java.sql.SQLException;
import recommendationsystem.controllers.DbManager;
import recommendationsystem.model.MethodHistory;

/**
 * PFISMatrix is the data representation of the PFIS topology. It is one of the
 * models driving recommendations available in this plug-in. It consists of
 * three {@link PFISNodeList} types: {@link PFISHistoryList},
 * {@link PFISWordList} and {@link PFISMethodList}. Each of the nodes is linked
 * in the sequentially as follows:
 * <ul>
 * <li>History nodes link to word nodes.
 * <li>A method in the PFISHistoryList has a link for every instance of a word
 * to the corresponding node in the PFISWordList.
 * <li>Word nodes link to method nodes.
 * <li>A word node has only one link to any method in PFISMethodList.
 * </ul>
 * <p>
 * There is an option to run PFIS without history in which case spreading
 * activation only occurs starting at the current location.
 * <p>
 * PFISMatrix makes its recommendations by spreading activation from the
 * PFISHistoryList to the PFISWordList and then to the PFISMethodList. Of all
 * the methods that were activated in the PFISMethodList, the top N are returned
 * as recommendations.
 * <p>
 * PFISMatrix is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISMatrix
{
    private static PFISMatrix instance;

    private PFISHistoryList historyList;
    private PFISWordList wordList;
    private PFISMethodList methodList;
    private DbManager dbManager;
    private ResultSet rs;

    /**
     * The singleton constructor.
     */
    private PFISMatrix()
    {
        // PFISMatrix is singleton, don't put anything here
    }

    /**
     * Gets an instance of PFISMatrix.
     * 
     * @return The singleton instance of PFISMatrix.
     */
    public static synchronized PFISMatrix getInstance()
    {
        if (instance == null)
        {
            instance = new PFISMatrix();
            instance.init();
        }
        return instance;
    }

    /**
     * PFISMatrix is singleton. Don't allow clones.
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
        historyList = PFISHistoryList.getInstance();
        wordList = PFISWordList.getInstance();
        methodList = PFISMethodList.getInstance();
        dbManager = DbManager.getInstance();
    }

    /**
     * Resets all the data structures. This clears all the nodes in the
     * topology.
     */
    public void reset()
    {
        historyList.clear();
        wordList.clear();
        methodList.clear();
    }

    /**
     * Clears the current PFISHistoryList and copies the current MethodHistory
     * into the PFISHistoryList, adding all the necessary word links along the
     * way.
     */
    public void setHistory()
    {
        MethodHistory hist = MethodHistory.getInstance();
        historyList.clear();
        for (String methodKey : hist.getMethodHistory())
        {
            addToHistory(methodKey);
        }
    }

    /**
     * Returns the PFISWordList from the current topology.
     * 
     * @return A pointer to the PFISWordList
     */
    public PFISNodeList getWordList()
    {
        return wordList;
    }

    /**
     * Adds the given methodKey to the history in PFISHistoryList. Each method
     * added is given a starting weight of 1 and has the current word nodes from
     * PFISWordList properly connected.
     * 
     * @param methodKey
     *            The current method's key
     */
    public void addToHistory(String methodKey)
    {
        PFISNode node = historyList.addNode(methodKey);
        node.setWeight(1);
        int methodId = dbManager.getMethodIdFromMethodKey(methodKey);
        String word = null;

        try
        {
            rs = dbManager.getWordsFromMethodId(methodId);
            while (rs.next())
            {
                word = rs.getString(2);
                node.addChild(wordList.getNode(word));
            }
            rs.close();
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Resets all the weights in the topology to 1.
     */
    private void resetWeights()
    {
        historyList.resetWeights();
        wordList.resetWeights();
        methodList.resetWeights();
    }

    /**
     * Runs the spreading activation algorithm, starting in the PFISHistory
     * list. Spreading will continue through the PFISWordList and then the
     * PFISMethodList. Activation will only spread for the given number of nodes
     * from the current method up to the number of nodes most recently in the
     * past.
     * 
     * @param numSpreadingNodes
     *            The number of nodes to spread from
     */
    private void spreadActivation(int numSpreadingNodes)
    {
        historyList.decayList(0.9F, numSpreadingNodes);
        historyList.spreadActivation(0.85F, numSpreadingNodes);
    }

    /**
     * Returns up to the top N recommendations by running the PFIS algorithm
     * over the topology. This method is synchronized.
     * 
     * @param methodKey
     *            The current method's key
     * @param numToRecommend
     *            The maximum number of recommendations to return
     * @param numToSpreadFrom
     *            The number of history nodes to start spreading from
     * @return A string array of method keys which represents an ordered list or
     *         recommendations. The zero index is the highest recommendation.
     *         Note that this array may be smaller than numToRecommend.
     */
    public synchronized String[] getTopNRecommendations(String methodKey,
            int numToRecommend, int numToSpreadFrom)
    {
        resetWeights();
        spreadActivation(numToSpreadFrom);

        //return methodList.getTopNMethods(methodKey, numToRecommend);
        return methodList.getTopNMethodsHalfAndHalf(methodKey, numToRecommend);
    }

    /**
     * Adds a word to the PFISWordList in the current topology. If the word does
     * not exist in the list, the pointer to the PFISNode points to the new
     * node. If the word exists, then a pointer points to the existing node.
     * 
     * @param word
     *            The word to add to the list
     * @return A pointer to the PFISNode for that word
     */
    public PFISNode addWordToWordList(String word)
    {
        return wordList.addNode(word);
    }

    /**
     * Adds a method to the PFISMethodList in the current topology. If the
     * method does not exist in the list, the pointer to the PFISNode points to
     * the new node. If the method exists, then a pointer points to the existing
     * node.
     * 
     * @param methodKey
     *            The method's key to add to the list
     * @return A pointer to the PFISNode for that method
     */
    public PFISNode addMethodToMethodList(String methodKey)
    {
        return methodList.addNode(methodKey);
    }
}
