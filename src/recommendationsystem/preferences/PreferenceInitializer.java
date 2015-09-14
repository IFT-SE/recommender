package recommendationsystem.preferences;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import recommendationsystem.Activator;
import recommendationsystem.controllers.ModelManager.ModelType;

/**
 * PreferenceInitializer initializes all values to a default in the plug-in's
 * preference store.
 * 
 * @author David Piorkowski
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    /**
     * Initializes all the default preferences for the plug-in preference pane.
     * These defaults appear on the first run of the plug-in or whenever the set
     * defaults button is clicked.
     */
    @Override
    public void initializeDefaultPreferences()
    {
        String eclipsePath = new Path(Platform.getInstallLocation().getURL()
                .getPath()).addTrailingSeparator().makeAbsolute().toOSString();
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        prefs.setDefault(PreferenceKeys.DB_CONNECT_STRING,
                "jdbc:mysql://localhost/ift?user=root&password=godiva12");
        prefs.setDefault(PreferenceKeys.ERROR_LOGGER_PATH, eclipsePath
                + "rs_errorLogger.txt");
        prefs.setDefault(PreferenceKeys.GUI_SHOW_DB_LOADER, false);
        prefs.setDefault(PreferenceKeys.GUI_SHOW_LOG_PLAYBACK, false);
        prefs.setDefault(PreferenceKeys.GUI_SHOW_SAVE_CONTROLS, false);
        prefs.setDefault(PreferenceKeys.LOGGER_PATH, eclipsePath
                + "rs_logger.txt");
        prefs.setDefault(PreferenceKeys.MODEL_ENABLE_HISTORY, true);
        prefs.setDefault(PreferenceKeys.MODEL_ENABLE_WORDS, true);
        prefs.setDefault(PreferenceKeys.MODEL_LOAD, false);
        prefs.setDefault(PreferenceKeys.MODEL_LOAD_PATH, eclipsePath
                + "rs_pfis.sav");
        prefs.setDefault(PreferenceKeys.MODEL_SAVE_PATH, eclipsePath
                + "rs_pfis.sav");
        prefs.setDefault(PreferenceKeys.MODEL_TYPE, ModelType.PFIS.toString());
        prefs.setDefault(PreferenceKeys.NUM_RECOMMENDATIONS, 10);
    }

}
