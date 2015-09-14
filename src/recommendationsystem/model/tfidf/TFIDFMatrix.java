package recommendationsystem.model.tfidf;

import java.util.Set;

import recommendationsystem.model.MethodHistory;

/**
 * TFIDFMatrix is the data representation of the TF-IDF cosine similarity
 * matrix. It is one of the models driving recommendations available in this
 * plug-in. The cosine similarity matrix measures the similarity between any two
 * methods using TF-IDF as its metric.
 * <p>
 * Each column of the matrix represents a method's similarity to every other
 * method in the source code. Method's are indexed and the indices are stored in
 * the methodIndex field variable.
 * <p>
 * This model can be run with or without history. Without history, the result is
 * simply a lookup in the matrix. With history, the column of the each method is
 * decayed and added to a result which is then used for the lookup. See
 * getCombinedHistoryColumn(float, int) for the details. When using history,
 * TFIDFMatrix assumes that {@link MethodHistory} is accurate.
 * <p>
 * PFISMatrix is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class TFIDFMatrix
{
    private static TFIDFMatrix instance;
    private MethodHistory methodHistory;

    private float[][] cosineMatrix;
    private String[] methodIndex;

    /**
     * The singleton constructor
     */
    private TFIDFMatrix()
    {
        // TFIDFMatrix is singleton, do not put anything here
    }

    /**
     * Returns an instance of TFIDFMatrix.
     * 
     * @return The singleton instance of TFIDFMatrix
     */
    public static synchronized TFIDFMatrix getInstance()
    {
        if (instance == null)
        {
            instance = new TFIDFMatrix();
            instance.init();
        }
        return instance;
    }

    /**
     * TFIDFMatrix is singleton. Don't allow clones.
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
        methodHistory = MethodHistory.getInstance();
    }

    /**
     * Resets all the data structures. This clears sets the cosine matrix and
     * method index to null.
     */
    public void reset()
    {
        cosineMatrix = null;
        methodIndex = null;
    }

    /**
     * Returns an array ordered by the order specified in the method index. This
     * array combines the similarity scores from the number of steps given from
     * the current location using the decay given. This gives the final score
     * for use in determining the top N recommendations when using TF-IDF with
     * history.
     * 
     * @param decay
     *            The factor to decay by, between 0 and 1
     * @param numSteps
     *            The number of steps in the history to look up
     * @return An array of similarity scores representing the combined history
     */
    private float[] getCombinedHistoryColumn(float decay, int numSteps)
    {
        float totalDecay = 1;
        int methodIndex;
        float[] rv = new float[cosineMatrix.length];
        String methodKey;

        for (int i = methodHistory.length() - 1; i > -1; i--)
        {
            methodKey = methodHistory.getMethodKeyAtStep(i);
            if (methodKey != null)
            {
                methodIndex = getMethodIndex(methodKey);
                if (methodIndex > -1)
                {
                    for (int j = 0; j < cosineMatrix.length; j++)
                    {
                        rv[j] = cosineMatrix[methodIndex][j] * totalDecay
                                + rv[j];
                    }
                    totalDecay = totalDecay * decay;
                }
            }
        }

        return rv;
    }

    /**
     * Returns the index in the cosine similarity matrix for the given method's
     * key.
     * 
     * @param methodKey
     *            The method's key to seek
     * @return <ul>
     *         <li>The index if the method's key exists
     *         <li>-1 if the key is not found
     *         </ul>
     */
    private int getMethodIndex(String methodKey)
    {
        for (int index = 0; index < methodIndex.length; index++)
        {
            if (methodIndex[index].equals(methodKey))
                return index;
        }
        return -1;
    }

    /**
     * Returns the top N recommendations with history using the TF-IDF cosine
     * similarity matrix. The top recommendation is located in the zero index.
     * This method is synchronized.
     * 
     * @param methodKey
     *            The current method's key
     * @param numToRecommend
     *            The number of recommendations
     * @param numHistorySteps
     *            The number of steps in the history to consider
     * @return A string array of method keys which represents an ordered list or
     *         recommendations. The zero index is the highest recommendation.
     *         Note that this array may be smaller than numToRecommend.
     */
    public synchronized String[] getTopNRecommendations2(String methodKey,
            int numToRecommend, int numHistorySteps)
    {
        // TODO: validate correctness
        // Find the index of the method
        int index = getMethodIndex(methodKey);

        float tMax = 0;
        int tIndex = 0;
        // System.out.println("len: " + methodIndex.length);
        System.out
                .println("methodIndex[" + index + "] = " + methodIndex[index]);

        // Temporary storage for our values
        int tempIndex[] = new int[numToRecommend];
        float tempVal[] = new float[numToRecommend];
        int numRecommendations = 0;
        float[] cosineColumn = getCombinedHistoryColumn(0.9F, numHistorySteps);

        // Iterate over the array and store our ten largest values
        for (int i = 0; i < numToRecommend; i++)
        {
            for (int j = 0; j < cosineColumn.length; j++)
            {
                if (index != j && cosineColumn[j] > tMax)
                {
                    tMax = cosineColumn[j];
                    tIndex = j;
                }
            }

            if (tMax == 0)
                break;
            // We store and negate values so they don't get caught in the next
            // iteration of the loop
            cosineColumn[tIndex] = -cosineColumn[tIndex];
            tempIndex[i] = tIndex;
            tempVal[i] = tMax;
            tMax = 0;
            numRecommendations++;
        }

        String[] rv = new String[numRecommendations];
        // Restore our previously negated values and fill our return array
        for (int i = 0; i < numRecommendations; i++)
        {
            rv[i] = methodIndex[tempIndex[i]];
        }

        return rv;
    }

    /**
     * Returns the top N recommendations with history using the TF-IDF cosine
     * similarity matrix. This method tries to return results such that half are
     * unvisited methods and half are visited. The top recommendation is located
     * in the zero index. This method is synchronized.
     * 
     * @param methodKey
     *            The current method's key
     * @param numToRecommend
     *            The number of recommendations
     * @param numHistorySteps
     *            The number of steps in the history to consider
     * @return A string array of method keys which represents an ordered list or
     *         recommendations. The zero index is the highest recommendation.
     *         Note that this array may be smaller than numToRecommend.
     */
    public synchronized String[] getTopNRecommendations2HalfAndHalf(
            String methodKey, int numToRecommend, int numHistorySteps)
    {
        // Find the index of the method
        int index = getMethodIndex(methodKey);

        Set<String> uniqueVisited = MethodHistory.getInstance()
                .getUniqueVisitedMethods();

        // Determine how many visited and unvisited methods to recommend
        // Don't include the current method, subtract 1
        int numVisited = uniqueVisited.size() - 1;
        int numUnvisited = numToRecommend / 2 + numToRecommend % 2;

        if (numVisited <= numToRecommend / 2)
            numUnvisited = numToRecommend - numVisited;
        else
            numVisited = numToRecommend / 2;

        System.out.println("Unvisited = " + numUnvisited + " Visited = "
                + numVisited);

        float tMax = 0;
        int tIndex = 0;
        boolean visited = false;

        // Temporary storage for our values
        int tempIndex[] = new int[numToRecommend];
        float tempVal[] = new float[numToRecommend];
        int numRecommendations = 0;
        float[] cosineColumn = getCombinedHistoryColumn(0.9F, numHistorySteps);

        // Iterate over the array and store our ten largest values
        for (int i = 0; i < numToRecommend; i++)
        {
            for (int j = 0; j < cosineColumn.length; j++)
            {
                if (index != j && cosineColumn[j] > tMax)
                {
                    visited = uniqueVisited.contains(methodIndex[j]);
                    if ((visited && numVisited > 0)
                            || (!visited && numUnvisited > 0))
                    {
                        tMax = cosineColumn[j];
                        tIndex = j;
                    }
                }
            }

            if (tMax == 0)
                break;
            // We store and negate values so they don't get caught in the next
            // iteration of the loop
            cosineColumn[tIndex] = -cosineColumn[tIndex];
            tempIndex[i] = tIndex;
            tempVal[i] = tMax;
            tMax = 0;
            numRecommendations++;
            if (visited)
                numVisited--;
            else
                numUnvisited--;
        }
        System.out.println("Recommended: " + numRecommendations);
        System.out.println("Unvisited Left: " + numUnvisited
                + " Visited Left: " + numVisited);

        String[] rv = new String[numRecommendations];
        // Restore our previously negated values and fill our return array
        for (int i = 0; i < numRecommendations; i++)
        {
            rv[i] = methodIndex[tempIndex[i]];
        }

        return rv;
    }

    /**
     * Returns the top N recommendations without history using the TF-IDF cosine
     * similarity matrix. The top recommendation is located in the zero index.
     * This method is synchronized.
     * 
     * @param methodKey
     *            The current method's key
     * @param n
     *            The number of recommendations
     * @return A string array of method keys which represents an ordered list or
     *         recommendations. The zero index is the highest recommendation.
     *         Note that this array may be smaller than numToRecommend.
     */
    public synchronized String[] getTopNRecommendations(String methodKey, int n)
    {
        // Find the index of the method
        int index = getMethodIndex(methodKey);

        float tMax = 0;
        int tIndex = 0;
        // System.out.println("len: " + methodIndex.length);
        System.out
                .println("methodIndex[" + index + "] = " + methodIndex[index]);

        // Temporary storage for our values
        int tempIndex[] = new int[n];
        float tempVal[] = new float[n];
        int numRecommendations = 0;

        // Iterate over the array and store our ten largest values
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < cosineMatrix.length; j++)
            {
                if (index != j && cosineMatrix[index][j] > tMax)
                {
                    tMax = cosineMatrix[index][j];
                    tIndex = j;
                }
            }

            if (tMax == 0)
                break;
            // We store and negate values so they don't get caught in the next
            // iteration of the loop
            cosineMatrix[index][tIndex] = -cosineMatrix[index][tIndex];
            tempIndex[i] = tIndex;
            tempVal[i] = tMax;
            tMax = 0;
            numRecommendations++;
        }

        String[] rv = new String[numRecommendations];
        // Restore our previously negated values and fill our return array
        for (int i = 0; i < numRecommendations; i++)
        {
            cosineMatrix[index][tempIndex[i]] = -cosineMatrix[index][tempIndex[i]];
            rv[i] = methodIndex[tempIndex[i]];
        }

        return rv;
    }

    /**
     * Sets the mapping from method keys to indices in the cosine similarity
     * matrix.
     * 
     * @param mIndex
     *            An array of methods' keys where each key's index corresponds
     *            to its position in the cosine similarity matrix.
     */
    public void setMethodIndex(String[] mIndex)
    {
        methodIndex = mIndex;
    }

    /**
     * Sets the cosine similarity matrix.
     * 
     * @param simMatrix
     *            A 2 dimensional array when each intersection of indices is a
     *            similarity score between the method in the first index to the
     *            method in the second index as specified by methodIndex.
     */
    public void setCosineSimilarityMatrix(float[][] simMatrix)
    {
        cosineMatrix = simMatrix;
    }

    /**
     * Returns the mapping from method keys to indices in the cosine similarity
     * matrix.
     * 
     * @return A pointer to the methodIndex field variable.
     */
    public String[] getMethodIndex()
    {
        return methodIndex;
    }

    /**
     * Returns the cosine similarity matrix.
     * 
     * @return A pointer to the cosineMatrix field variable
     */
    public float[][] getCosineSimilarityMatrix()
    {
        return cosineMatrix;
    }

}
