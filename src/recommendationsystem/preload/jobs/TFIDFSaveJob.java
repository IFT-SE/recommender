package recommendationsystem.preload.jobs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import recommendationsystem.Activator;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.model.tfidf.TFIDFMatrix;

/**
 * TFIDFSaveJob saves the in memory representation of the TFIDFMatrix to a text
 * file. It does not store any history, just the cosine similarity scores and
 * the method indices.
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
public class TFIDFSaveJob extends Job
{
    private String fileName;
    private ErrorLogger eLog;

    /**
     * The constructor.
     * 
     * @param name
     *            The name of this job
     * @param fileName
     *            The path of the file to save to.
     */
    public TFIDFSaveJob(String name, String fileName)
    {
        super(name);
        this.fileName = fileName;
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Saves the TF-IDF cosine similarity matrix and method indices to a file.
     * This will overwrite any existing file at the given location.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        Writer out = null;
        String newLine = System.getProperty("line.separator");
        int i = 0, j = 0;
        String[] methodIndex = TFIDFMatrix.getInstance().getMethodIndex();
        float[][] cosineMatrix = TFIDFMatrix.getInstance()
                .getCosineSimilarityMatrix();
        try
        {
            out = new OutputStreamWriter(new FileOutputStream(fileName));
            int len = methodIndex.length;
            monitor.beginTask("Saving the TF-IDF cosine similarity matrix.",
                    len);
            out.write(String.valueOf(len));
            out.write(newLine);
            for (i = 0; i < len - 1; i++)
            {
                out.write(methodIndex[i] + '\t');
            }
            out.write(String.valueOf(methodIndex[len - 1]) + newLine);

            for (i = 0; i < len; i++)
            {
                for (j = 0; j < len - 1; j++)
                {
                    out.write(String.valueOf(cosineMatrix[i][j]) + '\t');
                }
                out.write(String.valueOf(cosineMatrix[i][len - 1]) + newLine);
                monitor.worked(1);

                if (monitor.isCanceled())
                {
                    return new Status(Status.ERROR, Activator.PLUGIN_ID,
                            "The TF-IDF cosine similarity matrix was not saved. User cancelled.");
                }
            }
            return Status.OK_STATUS;
        }
        catch (IOException e)
        {
            eLog.logException(e);
            return new Status(Status.ERROR, Activator.PLUGIN_ID,
                    "There was IO error while saving the TF-IDF cosine similarity matrix.");
        }
        finally
        {
            monitor.done();
            System.out.println("TF-IDF save complete.");
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                eLog.logException(e);
            }
        }
    }
}
