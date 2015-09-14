package recommendationsystem.preload.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import recommendationsystem.preload.DatabaseLoader;

/**
 * DatabaseLoadJob wraps {@link DatabaseLoader} in a job so that we can see
 * progress as the database is being populated.
 * <p>
 * Unlike the other jobs, this one requires an ugly hack to report progress
 * which breaks from the correct way to use monitors. This is mainly due to ASTs
 * using the visitor pattern.
 * 
 * @author David Piorkowski
 * 
 */
public class DatabaseLoadJob extends Job
{
    /**
     * The constructor.
     * 
     * @param name
     *            The name of the job
     */
    public DatabaseLoadJob(String name)
    {
        super(name);
    }

    /**
     * Loads all the necessary data from the source code files into the IFT
     * database for use with the plug-in. See {@link DatabaseLoader} for more
     * details. This allows DatabaseLoader to be run on its own thread and have
     * progress tracked.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        try
        {
            // Stupid visitor enforces this hack
            DatabaseLoader.getInstance().start(monitor);
            return Status.OK_STATUS;
        }
        finally
        {
            monitor.done();
        }
    }
}
