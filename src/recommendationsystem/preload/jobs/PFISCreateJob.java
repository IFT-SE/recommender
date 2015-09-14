package recommendationsystem.preload.jobs;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import recommendationsystem.Activator;
import recommendationsystem.controllers.DbManager;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.model.pfis.PFISMatrix;
import recommendationsystem.model.pfis.PFISNode;

/**
 * PFISCreateJob reads the content in the IFT database and creates and instance
 * of the PFIS topology in memory. This job is meant to be run after a project
 * has been loaded into the database using {@link DatabaseLoadJob}. After this
 * job is run, {@link PFISMatrix} will be formed and have the correct topology
 * with a reset history. From there, it is recommended that the topology is
 * saved using {@link PFISSaveJob} so the long loading process does not need to
 * be rerun later.
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
public class PFISCreateJob extends Job
{
    private ErrorLogger eLog;

    /**
     * The constructor.
     * 
     * @param name
     *            The name of the job
     */
    public PFISCreateJob(String name)
    {
        super(name);
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Loads the necessary content from the database to create the PFIS topology
     * and store it to PFISMatrix. After execution, an instance of PFISMatrix
     * exists and is ready for use.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        DbManager dbManager = DbManager.getInstance();
        PFISMatrix pfisMatrix = PFISMatrix.getInstance();
        pfisMatrix.reset();

        int currentWordId = 0;
        int numWords = dbManager.getNumWords();
        String currentMethodKey = null;
        String currentWord = null;
        PFISNode methodNode = null, wordNode = null;
        ResultSet rs = dbManager.getAllWordIdsAndWords();
        ResultSet rs2 = null;

        try
        {
            monitor.beginTask("Creating the PFIS Topology.", numWords);
            while (rs.next())
            {
                currentWordId = rs.getInt(1);
                currentWord = rs.getString(2);
                System.out.println("Proceesing " + currentWord + "...");

                wordNode = pfisMatrix.addWordToWordList(currentWord);
                rs2 = dbManager.getMethodsFromWordId(currentWordId);

                while (rs2.next())
                {
                    currentMethodKey = rs2.getString(2);
                    methodNode = pfisMatrix
                            .addMethodToMethodList(currentMethodKey);
                    wordNode.addChild(methodNode);
                }
                rs2.close();
                monitor.worked(1);

                if (monitor.isCanceled())
                {
                    return new Status(Status.ERROR, Activator.PLUGIN_ID,
                            "The topology must be loaded before using the plug-in. User cancelled.");
                }
            }
            rs.close();
            return Status.OK_STATUS;
        }
        catch (SQLException e)
        {
            eLog.logException(e);
            return new Status(Status.ERROR, Activator.PLUGIN_ID,
                    "There was an error loading the PFIS topology.");
        }
        finally
        {
            monitor.done();
            System.out.println("PFIS Topology creation complete.");
            try
            {
                if (rs != null && !rs.isClosed())
                    rs.close();
                if (rs2 != null && !rs.isClosed())
                    rs2.close();
            }
            catch (SQLException e)
            {
                eLog.logException(e);
            }
        }
    }
}
