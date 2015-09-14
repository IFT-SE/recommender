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
import recommendationsystem.model.MethodHistory;
import recommendationsystem.model.pfis.PFISMatrix;
import recommendationsystem.model.pfis.PFISNode;
import recommendationsystem.model.pfis.PFISNode.NodeType;

/**
 * PFISLoadJob reads in a PFIS save file and loads it into the
 * {@link PFISMatrix} instance. PFISLoadJob assumes that the file being passed
 * in is well-formed and does not verify its correctness. After this job is run,
 * {@link PFISMatrix} will be formed and have the correct topology with a reset
 * history.
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
public class PFISLoadJob extends Job
{
    private String fileName;
    private ErrorLogger eLog;

    /**
     * The constructor.
     * 
     * @param name
     *            The name of this job.
     * @param fileName
     *            The path of the file name to load from
     */
    public PFISLoadJob(String name, String fileName)
    {
        super(name);
        this.fileName = fileName;
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Loads the necessary content from the save file to create the PFIS
     * topology and stores it to PFISMatrix. After execution, an instance of
     * PFISMatrix exists and is ready for use.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        MethodHistory methodHistory = MethodHistory.getInstance();
        PFISMatrix pfisMatrix = PFISMatrix.getInstance();
        pfisMatrix.reset();

        Scanner sc = null;
        String buf = null;
        String[] tokens;
        PFISNode currWordNode = null, currMethodNode = null;
        int size = 0, histSize = 0;
        try
        {
            sc = new Scanner(new FileInputStream(fileName));
            histSize = methodHistory.length();
            size = Integer.valueOf(sc.nextLine().trim()) + histSize;

            monitor.beginTask(
                    "Loading the PFIS topology matrix.  Please wait until this load completes.",
                    size);
            // <listType> <nodeType> <nodeName> <nodeWeight>
            while (sc.hasNextLine())
            {
                buf = sc.nextLine().trim();
                tokens = buf.split("\t");
                if (tokens[0].equals(NodeType.WORD.toString()))
                {
                    currWordNode = pfisMatrix.addWordToWordList(tokens[1]);
                    monitor.worked(1);
                }
                else if (tokens[0].equals(NodeType.METHOD.toString()))
                {
                    currMethodNode = pfisMatrix
                            .addMethodToMethodList(tokens[1]);
                    currWordNode.addChild(currMethodNode);
                }

                if (monitor.isCanceled())
                {
                    return new Status(
                            Status.ERROR,
                            Activator.PLUGIN_ID,
                            "The matrix must be loaded before using the plug-in.  Please check the preferences and restart Eclipse");
                }
            }
            pfisMatrix.setHistory();
            monitor.worked(histSize);
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
            if (sc != null)
                sc.close();
            System.out.println("PFIS Loading complete");
        }
    }
}
