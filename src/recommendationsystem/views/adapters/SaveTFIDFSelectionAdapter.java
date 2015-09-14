package recommendationsystem.views.adapters;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import recommendationsystem.preferences.MyPreferences;
import recommendationsystem.preload.jobs.TFIDFSaveJob;

public class SaveTFIDFSelectionAdapter extends SelectionAdapter
{
    @Override
    public void widgetSelected(SelectionEvent e)
    {
        new TFIDFSaveJob("Save TF-IDF", MyPreferences.getInstance()
                .getPathToSaveModelTo().toOSString()).schedule();
    }
}