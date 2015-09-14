package recommendationsystem.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import recommendationsystem.Activator;
import recommendationsystem.controllers.ModelManager;
import recommendationsystem.controllers.ModelManager.ModelType;

/**
 * PreferencePage gives the plug-in a quick and dirty way to accept preferences
 * through the Eclipse interface without having to implement much. Preferences
 * appear under the Window -> Preferences menu option. In the preferences will
 * be an option called RecommendationSystem that contains all the preferences.
 * <p>
 * The convenience, however, comes with a price. Validating input is less than
 * ideal and the file fields require the file to actually exist, requiring users
 * to create a blank file to save to before selecting it. Furthermore, complex
 * validation such as verifying that a selected file to load from matches the
 * selected model type is currently more trouble than it's worth. With that in
 * mind, dealing with the interactions between preferences is left as an
 * exercise for the user. Some hints/warnings follow:
 * <ul>
 * <li>As already mentioned, you may have to create a file to save to for the
 * logger and model file before selecting it in the preferences.
 * <li>The plug-in assumes the database exists, is in the correct format, has
 * the correct permissions set. Without the correct database connection string,
 * this plug-in isn't going to do much.
 * <li>The logger will overwrite existing files for each new session of Eclipse.
 * The log will append actions until Eclipse is closed, even if models are
 * switched. When Eclipse reopens, that is when the log is reset.
 * <li>Most importantly of all make sure that if a TF-IDF model is selected,
 * that a TF-IDF save file path is correct. Same goes for PFIS and a PFIS file.
 * This is because certain preference changes may reload the model from the file
 * and or database.
 * <li>Limit selecting Load Model from File only when it's necessary, such as
 * the first time a model is selected.
 * <li>Changing any of the GUI controls require an Eclipse restart. Any of the
 * above warnings apply, especially ones about files being rewritten.
 * </ul>
 * <p>
 * So in short, the preference pain...ahem, pane is usable, but far from ideal
 * and prone to user error. Don't say I didn't warn you.
 * 
 * @author David Piorkowski
 * 
 */
public class PreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage
{
    /**
     * The constructor.
     */
    public PreferencePage()
    {
        super(FLAT);
        // setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("User Preferences for the Recommendation System Plug-in");
        MyPreferences.getInstance();
    }

    /**
     * Current implementation does nothing.
     */
    @Override
    public void init(IWorkbench workbench)
    {

    }

    /**
     * Creates a field editor for every preference located in
     * {@link PreferenceKeys}.
     */
    @Override
    protected void createFieldEditors()
    {
        addField(new StringFieldEditor(PreferenceKeys.DB_CONNECT_STRING,
                "MySQL Connection String (restart Eclipse)",
                getFieldEditorParent()));
        addField(new FileFieldEditor(PreferenceKeys.LOGGER_PATH, "Log File",
                getFieldEditorParent()));
        addField(new FileFieldEditor(PreferenceKeys.ERROR_LOGGER_PATH,
                "Error Log File", getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(PreferenceKeys.MODEL_TYPE,
                "Model Type", 1, new String[][]
                    {
                        { "PFIS", ModelType.PFIS.toString() },
                        { "TF-IDF", ModelType.TFIDF.toString() } },
                getFieldEditorParent()));
        addField(new IntegerFieldEditor(PreferenceKeys.NUM_RECOMMENDATIONS,
                "Number of recommendations to make", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceKeys.MODEL_ENABLE_HISTORY,
                "Enable history in models (active scent)",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceKeys.MODEL_ENABLE_WORDS,
                "Display cues in recommendations", getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceKeys.MODEL_LOAD,
                "Load Model from file", getFieldEditorParent()));
        addField(new FileFieldEditor(PreferenceKeys.MODEL_LOAD_PATH,
                "Model file to preload", getFieldEditorParent()));
        addField(new FileFieldEditor(PreferenceKeys.MODEL_SAVE_PATH,
                "Model preload save file", getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceKeys.GUI_SHOW_DB_LOADER,
                "Enable Database Preload Controls (Eclipse restart required)",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceKeys.GUI_SHOW_SAVE_CONTROLS,
                "Enable Model Save Controls (Eclipse restart required)",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceKeys.GUI_SHOW_LOG_PLAYBACK,
                "Enable Logger Playback Controls (Eclipse restart required)",
                getFieldEditorParent()));
        // this.performOk();
    }

    /**
     * We override this to make sure all calls that request the preference store
     * get the correct one.
     */
    @Override
    public IPreferenceStore doGetPreferenceStore()
    {
        return Activator.getDefault().getPreferenceStore();
    }

    /**
     * Checks if the model parameters have changed and sets the reload model
     * preference if it is deemed necessary. Specifically, it checks if any of
     * the following changed:
     * <ul>
     * <li>Model Type
     * <li>History Enabled
     * <li>Words Enabled
     * <li>The number of recommendations
     * <li>If the model should be loaded from a file
     * </ul>
     * This is performed when the user selects the OK button in the preferences.
     */
    @Override
    public boolean performOk()
    {
        IPreferenceStore prefs = getPreferenceStore();

        String oldModelType = prefs.getString(PreferenceKeys.MODEL_TYPE);
        boolean oldEnableHistory = prefs
                .getBoolean(PreferenceKeys.MODEL_ENABLE_HISTORY);
        boolean oldEnableWords = prefs
                .getBoolean(PreferenceKeys.MODEL_ENABLE_WORDS);
        int oldNumRecs = prefs.getInt(PreferenceKeys.NUM_RECOMMENDATIONS);

        boolean rv = super.performOk();
        String newModelType = prefs.getString(PreferenceKeys.MODEL_TYPE);
        boolean newLoadModel = prefs.getBoolean(PreferenceKeys.MODEL_LOAD);
        boolean newEnableHistory = prefs
                .getBoolean(PreferenceKeys.MODEL_ENABLE_HISTORY);
        boolean newEnableWords = prefs
                .getBoolean(PreferenceKeys.MODEL_ENABLE_WORDS);
        int newNumRecs = prefs.getInt(PreferenceKeys.NUM_RECOMMENDATIONS);

        // One of the model parameters has changed, we need to reload models
        if (!oldModelType.equals(newModelType) || (newLoadModel)
                || (oldEnableHistory != newEnableHistory && newEnableHistory))
        {
            // Enforce model reloading
            prefs.setValue(PreferenceKeys.MODEL_LOAD, true);
            ModelManager.getInstance().reset();
            // TODO: Model Load should be set to false after this completes
        }

        if (oldEnableWords != newEnableWords)
        {
            ModelManager.getInstance().setWordsEnabled(newEnableWords);
        }

        if (oldNumRecs != newNumRecs)
        {
            ModelManager.getInstance().setNumberOfRecommendation(newNumRecs);
        }

        return rv;
    }
}
