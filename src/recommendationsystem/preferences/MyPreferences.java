package recommendationsystem.preferences;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;

import recommendationsystem.Activator;
import recommendationsystem.controllers.ModelManager.ModelType;

/**
 * MyPreferences is a wrapper for the preference store of this plug-in. It
 * provides a convenient way to read all the preferences without having to deal
 * with the preference keys from {@link PreferenceKeys}.
 * <p>
 * MyPreferences is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class MyPreferences
{
    private static MyPreferences instance;
    private IPreferenceStore prefs;

    /**
     * The singleton constructor.
     */
    private MyPreferences()
    {
        // MyPreferences is singleton, do not put anything here.
    }

    /**
     * Gets an instance of MyPreferences.
     * 
     * @return Returns the singleton instance of MyPreferences
     */
    public synchronized static MyPreferences getInstance()
    {
        if (instance == null)
        {
            instance = new MyPreferences();
            instance.init();
        }
        return instance;
    }

    /**
     * MyPreferences is singleton. Don't allow clones.
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
        prefs = Activator.getDefault().getPreferenceStore();
    }

    /**
     * Returns the JDBC connection string to the MySQL/MariaDB database.
     * 
     * @return A connection string
     */
    public String getDatabaseConnectionString()
    {
        return prefs.getString(PreferenceKeys.DB_CONNECT_STRING);
    }

    /**
     * Returns True if the database pre-load controls are set to be visible.
     * This includes the buttons to load from the database into memory.
     * 
     * @return <ul>
     *         <li>True if the database pre-load controls are enabled
     *         <li>False otherwise
     *         </ul>
     */
    public boolean showDatabasePreLoadControls()
    {
        return prefs.getBoolean(PreferenceKeys.GUI_SHOW_DB_LOADER);
    }

    /**
     * Returns True if the log playback controls are set to be visible.
     * 
     * @return <ul>
     *         <li>True if the log playback controls are enabled
     *         <li>False otherwise
     *         </ul>
     */
    public boolean showLogPlaybackControls()
    {
        return prefs.getBoolean(PreferenceKeys.GUI_SHOW_LOG_PLAYBACK);
    }

    /**
     * Returns True if the model save controls are set to be visible.
     * 
     * @return <ul>
     *         <li>True if the model save controls are enabled
     *         <li>False otherwise
     *         </ul>
     */
    public boolean showModelSaveControls()
    {
        return prefs.getBoolean(PreferenceKeys.GUI_SHOW_SAVE_CONTROLS);
    }

    /**
     * Returns the path to save the error log to.
     * 
     * @return The log's file path
     */
    public String getErrorLoggerPath()
    {
        return prefs.getString(PreferenceKeys.ERROR_LOGGER_PATH);
    }

    /**
     * Returns the path to save the log to.
     * 
     * @return The log's file path
     */
    public String getLoggerPath()
    {
        return prefs.getString(PreferenceKeys.LOGGER_PATH);
    }

    /**
     * Returns True if history is enabled in the models. Defaults to a window
     * size of 10.
     * 
     * @return <ul>
     *         <li>True if the model history enabled
     *         <li>False otherwise
     *         </ul>
     */
    public boolean historyEnabled()
    {
        return prefs.getBoolean(PreferenceKeys.MODEL_ENABLE_HISTORY);
    }

    /**
     * Returns True if recommendations include words with recommendations.
     * 
     * @return <ul>
     *         <li>True if the cues are enabled
     *         <li>False otherwise
     *         </ul>
     */
    public boolean wordsEnabled()
    {
        return prefs.getBoolean(PreferenceKeys.MODEL_ENABLE_WORDS);
    }

    /**
     * Returns True if the model is to be loaded from the model path when the
     * preference window is closed. Note, there are instances where a model may
     * reload regardless of this setting.
     * 
     * @return <ul>
     *         <li>True if the model loading is enabled
     *         <li>False otherwise
     *         </ul>
     */
    public boolean loadModelFromSpecifiedPath()
    {
        return prefs.getBoolean(PreferenceKeys.MODEL_LOAD);
    }

    /**
     * Returns the file path to load the model from. This should be a TF-IDF
     * save file if the model type is TF-IDF and a PFIS file if the model type
     * is PFIS. However, there is no guarantee that this is the case as it is up
     * to the user to select the correct file.
     * 
     * @return
     */
    public Path getPathToLoadModelFrom()
    {
        return new Path(prefs.getString(PreferenceKeys.MODEL_LOAD_PATH));
    }

    /**
     * Returns the file path to save the model to. This should be a TF-IDF save
     * file if the model type is TF-IDF and a PFIS file if the model type is
     * PFIS. Regardless of which model is selected, this is the file where it
     * will be saved. It is up to the user to change this path if the model type
     * changes.
     * 
     * @return
     */
    public Path getPathToSaveModelTo()
    {
        return new Path(prefs.getString(PreferenceKeys.MODEL_SAVE_PATH));
    }

    /**
     * Returns which model is currently selected for providing recommendations.
     * 
     * @return <ul>
     *         <li>ModelType.PFIS if PFIS is selected
     *         <li>ModelType.TFIDF if TF-IDF is selected
     *         <li>null if some error occurs
     *         </ul>
     */
    public ModelType getCurrentlySelectedModel()
    {
        if (prefs.getString(PreferenceKeys.MODEL_TYPE).equals(
                ModelType.PFIS.toString()))
            return ModelType.PFIS;
        else if (prefs.getString(PreferenceKeys.MODEL_TYPE).equals(
                ModelType.TFIDF.toString()))
            return ModelType.TFIDF;

        return null;
    }

    /**
     * Returns the number of recommendations that are set to display.
     * 
     * @return The number of recommendations to display.
     */
    public int getNumberOfRecommendationsToDisplay()
    {
        return prefs.getInt(PreferenceKeys.NUM_RECOMMENDATIONS);
    }

    /**
     * Returns True if the model type is PFIS.
     * 
     * @return <ul>
     *         <li>True if ModelType.PFIS if PFIS is selected
     *         <li>False otherwise
     *         </ul>
     */
    public boolean PFISSelected()
    {
        return getCurrentlySelectedModel() == ModelType.PFIS;
    }

    /**
     * Returns True if the model type is TF-IDF.
     * 
     * @return <ul>
     *         <li>True if ModelType.TFIDF if TF-IDF is selected
     *         <li>False otherwise
     *         </ul>
     */
    public boolean TFIDFSelected()
    {
        return getCurrentlySelectedModel() == ModelType.TFIDF;
    }
}
