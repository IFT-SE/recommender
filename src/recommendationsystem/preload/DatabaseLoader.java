package recommendationsystem.preload;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import recommendationsystem.controllers.DbManager;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.preload.jobs.DatabaseLoadJob;
import recommendationsystem.preload.jobs.PFISCreateJob;
import recommendationsystem.preload.jobs.TFIDFCreateJob;
import recommendationsystem.visitors.CommentVisitor;
import recommendationsystem.visitors.MyASTVisitor;
import recommendationsystem.visitors.ResourceVisitor;

/**
 * DatabaseLoader is used to populate the database with all the necessary data
 * for the plug-in to make its recommendations. This should be run with only one
 * project open in the workspace as it is designed such that there is only one
 * database per project. Running it with multiple projects open will cause
 * invalid recommendations to appear.
 * <p>
 * Database loader assumes that all the tables in the IFT database has no data
 * in them and all the tables and stored procedures are properly formed. No
 * verification of database state is performed before insertion begins. It also
 * assumes that the project it is given compiles correctly in Eclipse. A SQL
 * file that will create such a table has been included under the db folder of
 * this project.
 * <p>
 * Executing DatabaseLoader is a long and time-consuming process which involves
 * the following steps:
 * <ul>
 * <li>Iterating through all the files in all open projects. (You should make
 * sure there in only one open project before starting DatabaseLoader).
 * <li>For each java file in the project, the following occurs:
 * <ul>
 * <li>The source code file is parsed into an Abstract Syntax Tree (AST).
 * <li>The unique key of each method declaration (which includes constructors)
 * is stored in the database as well as the file it belongs to.
 * <li>The words in each method declaration are processed and added to the
 * database. Processing includes, splitting camel-case words, stemming, and
 * ignoring stop words.
 * <li>Relationships between methods and words are determined and saved.
 * <li>Another pass of the source file grabs words from comments, processes them
 * and includes them in the method-word relationships if applicable.
 * </ul>
 * <li>Once all the data is in the database, model data should be created and
 * saved. See {@link PFISCreateJob} and {@link TFIDFCreateJob} for more details.
 * </ul>
 * <p>
 * DatabaseLoader is meant to be called using {@link DatabaseLoadJob}.
 * <p>
 * DatabaseLoader is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class DatabaseLoader
{
    private static DatabaseLoader instance;
    private DbManager dbManager;
    private ErrorLogger eLog;

    private List<IProject> openProjects;
    private IProgressMonitor monitor;

    /**
     * The singleton constructor
     */
    private DatabaseLoader()
    {
        // This class is singleton. Do not put anything here.
    }

    /**
     * Gets an instance of DatabaseLoader
     * 
     * @return Returns the singleton instance of DatabaseLoader
     */
    public static synchronized DatabaseLoader getInstance()
    {
        if (instance == null)
        {
            instance = new DatabaseLoader();
            instance.init();
        }
        return instance;
    }

    /**
     * DatabaseLoader is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Initialize the private variables.
     */
    private void init()
    {
        dbManager = DbManager.getInstance();
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Starts the database loading task. Since we're forced to use visitors,
     * there's no obvious way to extract this work the DatabaseLoadJob. Instead
     * we pass in the job's monitor and use it here. This is a hack and not a
     * good one since we don't do anything about a user requested cancel and
     * don't really push errors back to the monitor.
     * 
     * @param monitor
     *            The job's monitor to pass in. This monitor should be clean.
     */
    public void start(IProgressMonitor monitor)
    {
        this.monitor = monitor;
        openProjects = new ArrayList<IProject>();
        findOpenProjects();
        loadOpenProjectsIntoDb();
    }

    /**
     * Inform the monitor that we've worked one unit.
     */
    public synchronized void workedFile()
    {
        monitor.worked(1);
    }

    /**
     * Iterates through the list of projects and adds the first open project to
     * the list of projects
     */
    private void findOpenProjects()
    {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                .getProjects();
        for (IProject proj : projects)
        {
            // TODO: Still need to check if this is a java project or not
            if (proj.isAccessible())
            {
                openProjects.add(proj);
                break; // Only deal with one project for now
                // Support for more projects later...maybe
            }
        }
    }

    /**
     * Iterates through the list of open projects and fires a
     * {@link ResourceVisitor} over each one. The monitor is told that the job
     * has started and the visitor starts the process that populates the
     * database.
     */
    private void loadOpenProjectsIntoDb()
    {
        for (IProject proj : openProjects)
        {
            try
            {
                proj.open(null);
                monitor.beginTask("Filling IFT database with project content.",
                        countTotalJavaFiles(proj));
                proj.accept(new ResourceVisitor());
            }
            catch (CoreException e)
            {
                eLog.logException(e);
            }
        }
    }

    /**
     * Returns the total number of java files in the project by recursively
     * visiting directories.
     * 
     * @param resource
     *            The location to start counting from. This could be a project,
     *            folder or file
     * @return The total number of files with java-like extensions found.
     */
    private int countTotalJavaFiles(IResource resource)
    {
        if (resource == null)
        {
            return 0;
        }
        int size = 0;
        try
        {
            if (resource.getType() == IResource.FOLDER)
            {
                IFolder folder = (IFolder) resource;
                for (IResource file : folder.members())
                {
                    size += countTotalJavaFiles(file);
                }
            }
            else if (resource.getType() == IResource.PROJECT)
            {
                IProject proj = (IProject) resource;
                for (IResource file : proj.members())
                {
                    size += countTotalJavaFiles(file);
                }
            }
        }
        catch (CoreException e)
        {
            eLog.logException(e);
        }
        if (resource.getType() == IResource.FILE
                && JavaCore.isJavaLikeFileName(resource.getName()))
            size++;

        return size;

    }

    /**
     * Add a method's data, words and its mappings to the database. This method
     * is called externally from {@link MyASTVisitor}.
     * 
     * @param method
     *            A AST node of MethodDeclaration type
     */
    public void addMethodToDb(MethodDeclaration method)
    {
        System.out.println("Processing method: " + method.getName());

        CompilationUnit unit = (CompilationUnit) method.getRoot();
        IPath path = unit.getJavaElement().getResource().getLocation();

        dbManager.insertMethod(new MethodData(method.resolveBinding().getKey(),
                method.getName().toString(), path.toString()));

        // Gets camel case split words
        List<String> words = TFIDFIndex.getTokens(method.toString());

        for (String word : words)
        {
            addWordToDb(word, method.resolveBinding().getKey());
        }
    }

    /**
     * Add a comment's words and method mappings to the database. This method is
     * called externally from {@link CommentVisitor}.
     * 
     * @param cd
     *            The comment data to store
     */
    public void addCommentToDb(CommentData cd)
    {
        if (cd.getMethodKey() != null)
        {
            // Gets camel case split words
            List<String> words = TFIDFIndex.getTokens(cd.getComment());

            for (String word : words)
            {
                addWordToDb(word, cd.getMethodKey());
            }
        }
    }

    /**
     * Adds a word and its mapping to a method to the database. This is where
     * stop words are removed. It is here that stop words are discarded.
     * 
     * @param word
     *            The word to add to the database
     * @param methodKey
     *            The method's key that this word belongs to
     */
    private void addWordToDb(String word, String methodKey)
    {
        if (!StopWords.isStopWord(word))
        {
            dbManager.insertWord(word);

            int wordId = dbManager.getWordIdFromWord(word);
            int methodId = dbManager.getMethodIdFromMethodKey(methodKey);

            dbManager.insertMethodToWordMapping(methodId, wordId);

            int wordCount = dbManager.getWordCountFromMethodIdAndWordId(
                    methodId, wordId);
            if (wordCount == 0)
                dbManager.insertWordCountForMethodIdAndWordId(methodId, wordId,
                        1);
            else
                dbManager.updateWordCountForMethodIdAndWordId(methodId, wordId,
                        wordCount + 1);
        }
    }
}
