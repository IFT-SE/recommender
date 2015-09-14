package recommendationsystem.controllers;

import java.sql.SQLException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.TextSelection;

import recommendationsystem.controllers.threads.PFISThreadDispatcher;
import recommendationsystem.controllers.threads.TFIDFThreadDispatcher;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.logging.MyLogger;
import recommendationsystem.model.CurrentMethodList;
import recommendationsystem.model.MethodHistory;
import recommendationsystem.model.pfis.PFISMatrix;
import recommendationsystem.model.tfidf.TFIDFMatrix;
import recommendationsystem.preferences.MyPreferences;
import recommendationsystem.preload.jobs.PFISLoadJob;
import recommendationsystem.preload.jobs.TFIDFLoadJob;
import recommendationsystem.views.RecommendationSystemView;
import recommendationsystem.visitors.CurrentMethodsVisitor;

/**
 * ModelManager is used to manage updates of both either the PFIS or TF-IDF
 * model. It is also responsible for updating the CurrentMethodList, updating
 * the MethodHistory and updating the plugin's view. Model updates are handled
 * on separate threads ({@link PFISThreadDispatcher} and
 * {@link TFIDFThreadDispatcher}) so Eclipse does not lock while the models are
 * computing.
 * <p>
 * ModelManager expects that either a PFISMatrix or TFIDFMatrix has been
 * initialized and created before it is used.
 * <p>
 * ModelManager is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class ModelManager
{
    /**
     * 
     * Specifies which of the models is used to generate recommendations.
     * 
     */
    public enum ModelType
    {
        TFIDF, PFIS
    }

    private static ModelManager instance;
    private RecommendationSystemView view;
    private ModelType type;
    private MethodHistory methodHistory;
    private CurrentMethodList currentMethodList;
    private MyLogger log;
    private ErrorLogger eLog;
    private boolean historyEnabled;
    private boolean wordsEnabled;
    private int numRecommendations;

    /**
     * The singleton constructor.
     */
    private ModelManager()
    {
        // This class is singleton. Do not put anything here.
    }

    /**
     * Get the instance of ModelManager. By default, the model manager is set to
     * the TFIDF model. Use <code>setType(ModelType)</code> to change it.
     * 
     * @return A singleton instance of ModelManager
     */
    public static synchronized ModelManager getInstance()
    {
        if (instance == null)
        {
            instance = new ModelManager();
            instance.init();
        }
        return instance;
    }

    /**
     * ModelManager is singleton. Don't allow clones.
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
        currentMethodList = CurrentMethodList.getInstance();
        log = MyLogger.getInstance();
        eLog = ErrorLogger.getInstance();
        this.type = ModelType.TFIDF;
        numRecommendations = 10;
    }

    /**
     * Reset the ModelManager object. In the course of resetting, ModelManager
     * reads the preferences and runs the appropriate actions. This may cause
     * the models to reload from files into memory.
     * <p>
     * Note that this is a reset of the ModelManager, not of the models
     * themselves. We want to preserve history since this can be potentially
     * called mid-experiment.
     */
    public void reset()
    {
        MyPreferences prefs = MyPreferences.getInstance();
        MyLogger.getInstance().setFileName(prefs.getLoggerPath());
        eLog.setFileName(prefs.getErrorLoggerPath());

        try
        {
            DbManager.getInstance().openConnectionAndInit(
                    prefs.getDatabaseConnectionString());
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }

        // If the PFIS model is selected
        if (prefs.PFISSelected())
        {
            System.out.println("PFIS Selected");
            MyLogger.getInstance().logModelUsed(ModelType.PFIS,
                    prefs.historyEnabled());
            type = ModelType.PFIS;

            if (prefs.loadModelFromSpecifiedPath())
            {
                System.out.println("Loading PFIS Matrix from file...");
                new PFISLoadJob("PFIS Load", prefs.getPathToLoadModelFrom()
                        .toOSString()).schedule();
            }
        }
        // If the TF-IDF model is selected
        else if (prefs.TFIDFSelected())
        {
            System.out.println("TF-IDF Selected");
            MyLogger.getInstance().logModelUsed(ModelType.TFIDF,
                    prefs.historyEnabled());
            type = ModelType.TFIDF;

            if (prefs.loadModelFromSpecifiedPath())
            {
                System.out.println("Loading TF-IDF Matrix from file...");
                new TFIDFLoadJob("TF-IDF Load", prefs.getPathToLoadModelFrom()
                        .toOSString()).schedule();
            }
        }
        historyEnabled = prefs.historyEnabled();
        wordsEnabled = prefs.wordsEnabled();
        numRecommendations = prefs.getNumberOfRecommendationsToDisplay();
    }

    /**
     * Returns the currently set model type used for predictions.
     * 
     * @return the ModelType
     */
    public ModelType getType()
    {
        return type;
    }

    /**
     * Returns true if the user's navigation is used during recommendation.
     * 
     * @return <ul>
     *         <li>True if history is used in the predictive models
     *         <li>False otherwise
     *         </ul>
     */
    public boolean getHistoryEnabled()
    {
        return historyEnabled;
    }

    /**
     * Returns true if words are set to display in under each recommendation.
     * 
     * @return <ul>
     *         <li>True if words are set to display
     *         <li>False if recommendations will display without words
     *         </ul>
     */
    public boolean getWordsEnabled()
    {
        return wordsEnabled;
    }

    /**
     * Returns the number of recommendations the models seek.
     * 
     * @return The number of recommendations to seek
     */
    public int getNumberOfRecommendations()
    {
        return numRecommendations;
    }

    /**
     * Sets whether history should be used in recommendations.
     * 
     * @param enabled
     *            <ul>
     *            <li>True if history is used in the predictive models
     *            <li>False otherwise
     *            </ul>
     */
    public void setHistoryEnabled(boolean enabled)
    {
        historyEnabled = enabled;
    }

    /**
     * Sets whether words should be displayed in recommendations.
     * 
     * @param enabled
     *            <ul>
     *            <li>True if words are set to display
     *            <li>False if recommendations will display without words
     *            </ul>
     */
    public void setWordsEnabled(boolean enabled)
    {
        wordsEnabled = enabled;
    }

    /**
     * Sets the number of recommendations the models will seek. Note that this
     * number should be greater than zero.
     * 
     * @param numRecs
     *            The number of recommendations the models seek
     */
    public void setNumberOfRecommendation(int numRecs)
    {
        if (numRecs > 0)
            numRecommendations = numRecs;
    }

    /**
     * Sets the RecommendationSystemView that is used for updating the list.
     * This needs to be set before any calls to <code>updateModels()</code> is
     * made.
     * 
     * @param view
     *            A pointer to the RecommendationSystemView
     */
    public void setView(RecommendationSystemView view)
    {
        this.view = view;
    }

    /**
     * This method is the workhorse of the plug-in. It it called on every
     * TextSelection event in the Eclipse's source code editor view. It does the
     * following:
     * <ul>
     * <li>Determines the current method from the current text cursor location
     * <li>Updates MethodHistory if it is a new method location
     * <li>
     * Updates the current method.
     * <li>If the method determined is new, it runs the appropriate model based
     * on which ModelType is set.
     * <li>Models are spawned on new threads. See {@link PFISThreadDispatcher}
     * and {@link TFIDFThreadDispatcher} for more details. These threads then
     * update the view on completion.
     * </ul>
     * <p>
     * The following must be occur before updateModels is called:
     * <ul>
     * <li>DbManager exists and has a working connection.
     * <li>Either a {@link PFISMatrix} or {@link TFIDFMatrix} exist in memory
     * based on database data.
     * <li>The correct {@link ModelType} has been set.
     * </ul>
     * <p>
     * This method is synchronized.
     * 
     * @param selection
     * @param sourceFile
     */
    public synchronized void updateModels(TextSelection selection,
            IFile sourceFile)
    {
        // Determine the method the cursor is at
        MethodDeclaration method = getCurrentMethod(selection, sourceFile);

        // If we are in a new location that is a method
        if (method != null)
        {
            log.logTextCursorChange(selection.getOffset(), sourceFile.getName());
            String methodKey = method.resolveBinding().getKey();

            if (methodHistory.isEmpty()
                    || !methodKey
                            .equals(methodHistory.getMostRecentMethodKey()))
            {
                // Add method to method history
                methodHistory.addMethodKey(methodKey);

                // Update current method in the view
                view.updateCurrentMethod();

                // Disable the history table
                // view.setHistoryEnabled(false);

                if (type == ModelType.TFIDF)
                {
                    TFIDFThreadDispatcher tfidf = new TFIDFThreadDispatcher(
                            methodKey, view);
                    tfidf.start();
                }
                else if (type == ModelType.PFIS)
                {
                    PFISThreadDispatcher pfis = new PFISThreadDispatcher(
                            methodKey, view);
                    pfis.start();
                }
            }
        }
    }

    /**
     * Determines and returns a MethodDeclaration node based on the given file's
     * text cursor position.
     * 
     * @param selection
     *            The text selection captured by the listener
     * @param sourceFile
     *            The file the text selection refers to
     * @return <ul>
     *         <li>A MethodDeclaration node at the given location.
     *         <li>null if the text cursor is not in a method
     *         </ul>
     */
    private MethodDeclaration getCurrentMethod(TextSelection selection,
            IFile sourceFile)
    {
        if (sourceFile != currentMethodList.getCurrentSourceFile())
        {
            currentMethodList.clearData();
            currentMethodList.setCurrentSourceFile(sourceFile);
            fillCurrentMethodList(sourceFile);
        }

        MethodDeclaration node = currentMethodList
                .getMethodFromOffset(selection.getOffset());

        return node;
    }

    /**
     * Visits the AST from the given source code file and populates the
     * CurrentMethodList based on its contents.
     * 
     * @param sourceFile
     *            The source code file to generate the method list from.
     */
    private void fillCurrentMethodList(IFile sourceFile)
    {
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(sourceFile);
        ASTNode ast = parse(unit);
        ast.accept(new CurrentMethodsVisitor());
    }

    /**
     * Builds an AST from the given source file and returns it as a compilation
     * unit. This is later used to populate the CurrentMethodList.
     * 
     * @param unit
     *            The source code file to parse
     * @return CompilationUnit representing the passed in file
     */
    private CompilationUnit parse(ICompilationUnit unit)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null);
    }
}
