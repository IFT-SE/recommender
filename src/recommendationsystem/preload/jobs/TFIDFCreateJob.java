package recommendationsystem.preload.jobs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import recommendationsystem.Activator;
import recommendationsystem.controllers.DbManager;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.model.tfidf.TFIDFMatrix;

/**
 * TFIDFCreateJob reads the content in the IFT database and creates and instance
 * of the TF-IDF cosine similarity matrix in memory. This job is meant to be run
 * after a project has been loaded into the database using
 * {@link DatabaseLoadJob}. After this job is run, {@link TFIDFMatrix} will be
 * formed and have the correct similarity matrix with a reset history. From
 * there, it is recommended that the matrix is saved using {@link TFIDFSaveJob}
 * so the long loading process does not need to be rerun later.
 * <p>
 * Running the task in a job allows us to view progress in Eclipe's progress
 * view and the indicator at the lower-right corner of the interface. This gives
 * us visual feedback for when it is safe to continue using the tool.
 * <p>
 * <b>This job does not block the use of the plug-in. Do not use the plug-in
 * until this job is complete</b>
 * 
 * @author David Piorkowski
 * 
 */
public class TFIDFCreateJob extends Job
{
    private ErrorLogger eLog;

    /**
     * The constructor.
     * 
     * @param name
     *            The name of the job
     */
    public TFIDFCreateJob(String name)
    {
        super(name);
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Loads the necessary content from the database to create the TF-IDF cosine
     * similarity matrix and store it to TFIDFMatrix. After execution, an
     * instance of TFIDFMatrix exists and is ready for use.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        TFIDFMatrix tfidfMatrix = TFIDFMatrix.getInstance();
        DbManager dbManager = DbManager.getInstance();
        tfidfMatrix.reset();

        Map<String, Float> wordWeightVector;
        TreeMap<String, Map<String, Float>> matrix = new TreeMap<String, Map<String, Float>>();

        int currentMethodId = 0;
        int currentWordId = 0;
        int numOccCurrentWord = 0;
        int totMethods = 0;
        int totWordsCurrentMethod = 0;
        int totMethodsCurrentWord = 0;
        String currentMethodKey = null;
        String currentWord = null;
        ResultSet rs = null, rs2 = null;
        int i = 0, j = 0;
        float[][] cosineMatrix;

        // Get IDF Numerator - total number of methods
        totMethods = dbManager.getIdfNumerator();
        String[] methodIndex = new String[totMethods];

        monitor.beginTask("Creating the TFIDF Cosine Similarity Matrix.",
                2 * totMethods);

        rs = dbManager.getAllMethodIdsAndKeys();
        try
        {
            try
            {
                while (rs.next())
                {
                    currentMethodId = rs.getInt(1);
                    currentMethodKey = rs.getString(2);
                    System.out
                            .println("Processing " + currentMethodKey + "...");

                    // Populate our method indices
                    methodIndex[i++] = currentMethodKey;

                    // Create the word weight vector for the current method key
                    wordWeightVector = new TreeMap<String, Float>();

                    // Create a map for each of the method keys
                    matrix.put(currentMethodKey, wordWeightVector);

                    // Get TF Denominator - total number of words for the
                    // current method key
                    totWordsCurrentMethod = dbManager
                            .getTfDenominator(currentMethodId);
                    rs2 = dbManager.getWordsFromMethodId(currentMethodId);

                    while (rs2.next())
                    {

                        // The IDF Numerator is included in rs2 in the wordCount
                        // column
                        // rs2 = [id][word][wordCount]
                        // For each of the words that we have in rs2, we push
                        // them onto the word weight vector
                        currentWordId = rs2.getInt(1);
                        currentWord = rs2.getString(2);
                        numOccCurrentWord = rs2.getInt(3);

                        // For each word in the method, get IDF Denominator -
                        // number of methods current word appears
                        totMethodsCurrentWord = dbManager
                                .getIdfDenominator(currentWordId);

                        // We now have all the values we need
                        // TF-IDF = tf num / tf denom * idf num / idf denom
                        // Add them to the matrix
                        wordWeightVector.put(currentWord,
                                (float) numOccCurrentWord
                                        / totWordsCurrentMethod * totMethods
                                        / totMethodsCurrentWord);
                    }
                    rs2.close();
                    monitor.worked(1);

                    if (monitor.isCanceled())
                    {
                        return new Status(Status.ERROR, Activator.PLUGIN_ID,
                                "The matrix must be created before using the plug-in.  User cancelled.");
                    }
                }
                rs.close();
            }
            catch (SQLException e)
            {
                eLog.logException(e);
                return new Status(Status.ERROR, Activator.PLUGIN_ID,
                        "There was a database error when creating the TF-IDF cosine similarity matrix.");
            }

            // Calculate cosine similarity
            cosineMatrix = new float[methodIndex.length][methodIndex.length];
            System.out.println("Calculating cosine similarity...");
            for (i = 0; i < methodIndex.length; i++)
            {
                // for (j = i + 1; j < methodIndex.length; j++)
                for (j = 0; j < methodIndex.length; j++)
                {
                    cosineMatrix[i][j] = cosine(matrix.get(methodIndex[i]),
                            matrix.get(methodIndex[j]));
                }
                monitor.worked(1);

                if (monitor.isCanceled())
                {
                    return new Status(Status.ERROR, Activator.PLUGIN_ID,
                            "The matrix must be created before using the plug-in.  User cancelled.");
                }
            }

            tfidfMatrix.setCosineSimilarityMatrix(cosineMatrix);
            tfidfMatrix.setMethodIndex(methodIndex);
            return Status.OK_STATUS;
        }
        finally
        {
            monitor.done();
            System.out
                    .println("Creating TF-IDF cosine similiarity matrix complete.");
            try
            {
                if (rs != null && !rs.isClosed())
                    rs.close();
                if (rs2 != null && !rs2.isClosed())
                    rs2.close();
            }
            catch (SQLException e)
            {
                eLog.logException(e);
            }
        }
    }

    /**
     * Calculates the cosine similarity between two methods. Each method is
     * represented as a map from words in that method to their TF-IDF values. If
     * a word does not exist in the method, it is not included in the map.
     * 
     * @param v1
     *            The vector of the first method
     * @param v2
     *            The vector of the second method
     * @return A cosine-similarity score for v1 and v2
     */
    private float cosine(Map<String, Float> v1, Map<String, Float> v2)
    {
        if (v1.size() > v2.size())
        {
            Map<String, Float> tmp = v1;
            v1 = v2;
            v2 = tmp;
        }

        float sum = 0;
        for (String idx : v1.keySet())
        {
            float val1 = v1.get(idx);
            float val2 = v2.containsKey(idx) ? v2.get(idx) : 0F;
            sum += val1 * val2;
        }

        float norm1sq = 0;
        for (String idx : v1.keySet())
        {
            float t = v1.get(idx);
            norm1sq += t * t;
        }

        float norm2sq = 0;
        for (String idx : v2.keySet())
        {
            float t = v2.get(idx);
            norm2sq += t * t;
        }

        float rv = (float) (sum / (Math.sqrt(norm1sq) * Math.sqrt(norm2sq)));

        return rv;
    }

}
