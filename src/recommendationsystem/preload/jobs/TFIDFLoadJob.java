package recommendationsystem.preload.jobs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import recommendationsystem.Activator;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.model.tfidf.TFIDFMatrix;

/**
 * TFIDFLoadJob reads in a TF-IDF save file and loads it into the
 * {@link TFIDFMatrix} instance. TFIDFLoadJob assumes that the file being passed
 * in is well-formed and does not verify its correctness. After this job is run,
 * {@link TFIDFMatrix} will be formed and have the correct similarity matrix
 * with a reset history.
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
public class TFIDFLoadJob extends Job
{
    private String fileName;
    private ErrorLogger eLog;

    /**
     * The constructor.
     * 
     * @param name The name of this job.
     * @param fileName The path of the file name to load from
     */
    public TFIDFLoadJob(String name, String fileName)
    {
        super(name);
        this.fileName = fileName;
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Loads the necessary content from the save file to create the TF-IDF
     * cosine similarity matrix and stores it to TFIDFMatrix. After execution,
     * an instance of TFIDFMatrix exists and is ready for use.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        TFIDFMatrix tfidfMatrix = TFIDFMatrix.getInstance();
        tfidfMatrix.reset();

        Scanner sc = null;
        int size = 0, i = 0, j = 0;
        String buf = null;
        String[] tokens;
        try
        {
            sc = new Scanner(new FileInputStream(fileName));
            size = Integer.valueOf(sc.nextLine().trim());
            monitor.beginTask(
                    "Loading the TF-IDF cosine similarity matrix.   Please wait until this load completes.",
                    size);
            String[] methodIndex = new String[size];
            buf = sc.nextLine().trim();
            tokens = buf.split("\t");

            for (String token : tokens)
            {
                methodIndex[i++] = token;
            }

            float[][] cosineMatrix = new float[size][size];
            for (i = 0; i < size; i++)
            {
                j = 0;
                buf = sc.nextLine().trim();
                tokens = buf.split("\t");
                for (String token : tokens)
                {
                    cosineMatrix[i][j++] = Float.valueOf(token);
                }
                monitor.worked(1);
                if (monitor.isCanceled())
                {
                    return new Status(
                            Status.ERROR,
                            Activator.PLUGIN_ID,
                            "The matrix must be loaded before using the plug-in.  Please check the preferences and restart Eclipse");
                }
            }
            tfidfMatrix.setMethodIndex(methodIndex);
            tfidfMatrix.setCosineSimilarityMatrix(cosineMatrix);
            return Status.OK_STATUS;
        }
        catch (FileNotFoundException e)
        {
            eLog.logException(e);
            return new Status(Status.ERROR, Activator.PLUGIN_ID,
                    "There was a problem opening the file.");
        }
        finally
        {
            monitor.done();
            System.out.println("Loading TF-IDF matrix complete.");
            if (sc != null)
                sc.close();
        }
    }
}
