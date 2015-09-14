package recommendationsystem.views.adapters;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import recommendationsystem.preferences.MyPreferences;
import recommendationsystem.preload.jobs.PFISSaveJob;

public class SavePFISSelectionAdapter extends SelectionAdapter
{
    @Override
    public void widgetSelected(SelectionEvent e)
    {
        new PFISSaveJob("Save PFIS", MyPreferences.getInstance()
                .getPathToSaveModelTo().toOSString()).schedule();
    }
}