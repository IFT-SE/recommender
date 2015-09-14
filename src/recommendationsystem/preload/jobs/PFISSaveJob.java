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
import recommendationsystem.model.pfis.PFISMatrix;
import recommendationsystem.model.pfis.PFISNode;
import recommendationsystem.model.pfis.PFISNodeList;

/**
 * PFISSaveJob saves the in memory representation of the PFISMatrix to a text
 * file. It does not store any history or weights in the graphs, just the data
 * and the connections.
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
public class PFISSaveJob extends Job
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
    public PFISSaveJob(String name, String fileName)
    {
        super(name);
        this.fileName = fileName;
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Saves the PFIS topology sans history to a file. This will overwrite any
     * existing file at the given location.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        PFISNodeList wordList = PFISMatrix.getInstance().getWordList();
        String newLine = System.getProperty("line.separator");
        Writer out = null;
        try
        {
            out = new OutputStreamWriter(new FileOutputStream(fileName));
            int len = wordList.getSize();
            monitor.beginTask("Saving PFIS Topology", len);
            out.write(String.valueOf(len));
            out.write(newLine);

            for (PFISNode wordNode : wordList.getList())
            {
                out.write(wordNode.toString());
                out.write(newLine);
                for (PFISNode methodNode : wordNode.getChildren())
                {
                    out.write(methodNode.toString());
                    out.write(newLine);
                }
                monitor.worked(1);

                if (monitor.isCanceled())
                {
                    return new Status(Status.ERROR, Activator.PLUGIN_ID,
                            "The PFIS topology was not saved. User cancelled.");
                }
            }
            return Status.OK_STATUS;
        }
        catch (IOException e)
        {
            eLog.logException(e);
            return new Status(Status.ERROR, Activator.PLUGIN_ID,
                    "There was an IO error when saving the file.");
        }
        finally
        {
            monitor.done();
            System.out.println("Saving PFIS Topology complete.");
            try
            {
                if (out != null)
                    out.close();
            }
            catch (IOException e)
            {
                eLog.logException(e);
            }
        }
    }
}
